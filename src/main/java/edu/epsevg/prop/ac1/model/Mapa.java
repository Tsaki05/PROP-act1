package edu.epsevg.prop.ac1.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa l'estat del mapa: grid, posicions agents (indexades per id 1..n),
 * i bitmask de claus.
 *
 * Codifiquem:
 *  - PARET = -1
 *  - ESPAI = 0
 *  - SORTIDA = -2
 *  - claus: ascii 'a'..'z' (valors positius > 0)
 *  - portes: ascii 'A'..'Z' (valors positius > 0)
 */
public class Mapa {
    private final int n;
    private final int m;
    private final int[][] grid; // conservem caràcters ordinals o codis
    private final List<Posicio> agents; // agents indexats a partir de 1 (index 0 -> agent 1)
    private int clausMask;
    private Posicio sortida;
    
    /**
     * Definicions dels valors del grid: PARET
     */
    public static final int PARET = -1;
    /**
     * Definicions dels valors del grid: ESPAI
     */    
    public static final int ESPAI = 0;
    /**
     * Definicions dels valors del grid: SORTIDA
     */    
    public static final int SORTIDA = -2;

    /**
     * Constructor a partir d'un arxiu
     */
    public Mapa(Path fitxer) throws IOException {
        List<String> lines = Files.readAllLines(fitxer);
        this.n = lines.size();
        this.m = lines.get(0).length();
        this.grid = new int[n][m];
        this.agents = new ArrayList<>();
        this.clausMask = 0;
        
        sortida = null;
        
        for (int i = 0; i < n; i++) {
            String row = lines.get(i);
            for (int j = 0; j < m; j++) {
                char c = row.charAt(j);
                switch (c) {
                    case '#': grid[i][j] = PARET; break;
                    case ' ': grid[i][j] = ESPAI; break;
                    case '@': grid[i][j] = SORTIDA; sortida = new Posicio(i,j); break;
                    default:
                        if (Character.isDigit(c)) {
                            // posem l'agent, però __NO__ es situa a la graella
                            agents.add(new Posicio(i, j));
                            grid[i][j] = ESPAI;
                        } else if (Character.isLowerCase(c)) {
                            grid[i][j] = c; // desem directament la lletra
                        } else if (Character.isUpperCase(c)) {
                            grid[i][j] = c; // desem directament la lletra
                        } else {
                            grid[i][j] = ESPAI;
                        }
                }
            }
        }
        if(sortida==null) throw new RuntimeException("Sortida no definida.");
        if(agents.size()==0) throw new RuntimeException("Agents no definits.");
    }

    /** 
     * Constructor còpia 
     * Fa una "Deep copy" del mapa
     */
    public Mapa(Mapa other) {
        this.n = other.n;
        this.m = other.m;
        this.grid = new int[n][m];
        for (int i = 0; i < n; i++) System.arraycopy(other.grid[i], 0, this.grid[i], 0, m);
        
        this.agents = new ArrayList<>();
        for (Posicio p : other.agents) this.agents.add(new Posicio(p.x, p.y));
        this.clausMask = other.clausMask;
        this.sortida = other.sortida;  // IMPORTANT!
    }

    /**
     * Número de columnes
     * @return el nombre de columnes
     */
    public int getN() { return n; }
    
    /**
     * Número de files
     * @return el nombre de files
     */
    public int getM() { return m; }
    
    /**
     * @return Retorna la llista immutable de la posició dels agents
     */
    public List<Posicio> getAgents() { return Collections.unmodifiableList(agents); }
    
    /**
     * @return la màscara binària en format int de les claus. Cada clau és un bit, començant per la a (bit menys significant),b,c...
     *    P.ex. Si hi ha 3 claus, a, b i c, i tenim agafada la b i la c, la màscara val 6 (110 en binari)
     *          cba
     *          110 
     */
    public int getClausMask() { return clausMask; }

    /**
     * Permet saber si una posició conté la sortida
     * @return true si la posició és la sortida, false altrament
     */
    public boolean esSortida(Posicio p) {
        return getCell(p) == SORTIDA;
    }

    /**
     * @return el valor de la cella (veure constants PARET, ESPAI, SORTIDA)
     */
    private int getCell(Posicio p) {
        if (p.x < 0 || p.x >= n || p.y < 0 || p.y >= m) return PARET;
        return grid[p.x][p.y];
    }

    /**
     * Indicar que una clau ha estat recollida
     */
    private void setClauRecollida(char key) {
        int idx = key - 'a';
        clausMask |= (1 << idx);
    }

    /**
     * Permet saber si una clau ha estat recollida
     * @param key la clau que volem preguntar
     * @return true si la tenim
     */
    public boolean teClau(char key) {
        int idx = key - 'a';
        return (clausMask & (1 << idx)) != 0;
    }

    /**
     * Permet saber si podem obrir una clau determinada
     * @param door la porta que volem obrir (caràcter majúscules)
     * @return true si podem obrir-la
     */
    public boolean portaObrible(char door) {
        char key = Character.toLowerCase(door);
        return teClau(key);
    }

    /** 
     * Aplica el moviment SOBRE UNA CÒPIA (no altera el mapa actual)
     * @return  la nova instància amb el moviment ja fet.
     */
    public Mapa mou(Moviment acc) {
        Mapa nou = new Mapa(this);
        int aid = acc.getAgentId();
        if (aid < 1 || aid > nou.agents.size()) throw new IllegalArgumentException("Agent id invalid");
        Posicio actual = nou.agents.get(aid - 1);
        Posicio dest = actual.translate(acc.getDireccio());

        int cell = nou.getCell(dest);
        if (cell == PARET) throw new IllegalArgumentException("Moviment cap a mur");
        if (Character.isUpperCase(cell)) {
            // porta
            if (!nou.portaObrible((char) cell)) throw new IllegalArgumentException("Porta tancada");
        }
        // no permetre col·lisions
        for (int i = 0; i < nou.agents.size(); i++) {
            if (i == aid-1) continue;
            Posicio p = nou.agents.get(i);
            if (p.equals(dest)) throw new IllegalArgumentException("Colisio amb altre agent");
        }
        // aplicar moviment
        nou.agents.set(aid - 1, dest);
        // si hi ha clau i no la teniem, recollir-la
        if (Character.isLowerCase(cell)) {
            char key = (char) cell;
            if (!nou.teClau(key)) {
                // recollir
                nou.setClauRecollida(key);                
                nou.grid[dest.x][dest.y] = ESPAI;
            }
        }
        return nou;
    }

    /** 
     * Obtenir els moviments possibles des de l'estat actual
     * @return la llista de moviments possibles des de l'estat actual:
     *  - per cada agent (1..k) i cada direcció valida (que no sigui mur, si és una porta ha de ser obrible i sense col·lisió amb d'altres agents)
     *  - indica recullClau=true si el destí té una clau que encara no s'ha recollit
     */
    public List<Moviment> getAccionsPossibles() {
        List<Moviment> res = new ArrayList<>();
        
        // Per cada agent
        for (int agentId = 1; agentId <= agents.size(); agentId++) {
            Posicio posAgent = agents.get(agentId - 1);
            
            // Per cada direcció possible
            for (Direccio dir : Direccio.values()) {
                Posicio dest = posAgent.translate(dir);
                
                // Comprovar si el moviment és vàlid
                int cell = getCell(dest);
                
                // No podem moure'ns a una paret
                if (cell == PARET) continue;
                
                // Si és una porta, hem de tenir la clau
                if (Character.isUpperCase(cell)) {
                    if (!portaObrible((char) cell)) continue;
                }
                
                // Comprovar col·lisions amb altres agents
                boolean colisio = false;
                for (int i = 0; i < agents.size(); i++) {
                    if (i == agentId - 1) continue;
                    if (agents.get(i).equals(dest)) {
                        colisio = true;
                        break;
                    }
                }
                if (colisio) continue;
                
                // Determinar si recollim una clau
                boolean recullClau = false;
                if (Character.isLowerCase(cell)) {
                    char key = (char) cell;
                    if (!teClau(key)) {
                        recullClau = true;
                    }
                }
                
                // Afegir el moviment vàlid
                res.add(new Moviment(agentId, dir, recullClau));
            }
        }
        
        return res;
    }

    /** 
     * Permet saber si algú ha arribat a la sortida
     * @return true si algun agent ha arribat a la sortida 
     */
    public boolean esMeta() {
        for (Posicio p : agents) if (esSortida(p)) return true;
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Mapa other = (Mapa) o;
        
        // Comparar màscara de claus (molt ràpid)
        if (this.clausMask != other.clausMask) return false;
        
        // Comparar posicions dels agents (ràpid)
        if (this.agents.size() != other.agents.size()) return false;
        for (int i = 0; i < this.agents.size(); i++) {
            if (!this.agents.get(i).equals(other.agents.get(i))) return false;
        }
        
        // NO cal comparar tot el grid!
        // El grid només canvia quan recollim claus, i això ja està reflectit
        // en clausMask. Les parets i portes són constants del mapa.
        
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        
        // Hash de les posicions dels agents
        for (Posicio p : agents) {
            result = 31 * result + p.hashCode();
        }
        
        // Hash de la màscara de claus
        result = 31 * result + clausMask;
        
        // NO recorrem el grid - això és massa lent!
        // Els agents + clausMask ja identifiquen únicament l'estat
        
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Agents:");
        for (int i = 0; i < agents.size(); i++) sb.append(" ").append(i+1).append(agents.get(i));
        sb.append(" clausMask=").append(Integer.toBinaryString(clausMask));
        return sb.toString();
    }

    /** 
     * @return la posició de sortida del mapa
     */
    public Posicio getSortidaPosicio() {
        return sortida;
    }

    
    //===================================================================
    // Aquí van les vostres ampliacions (nous mètodes d'utilitat)
    //===================================================================
    
    /**
     * Retorna totes les claus que encara no han estat recollides
     * @return llista de posicions de claus pendents
     */
    public List<Posicio> getClausPendents() {
        List<Posicio> clausPendents = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int cell = grid[i][j];
                if (Character.isLowerCase(cell)) {
                    char key = (char) cell;
                    if (!teClau(key)) {
                        clausPendents.add(new Posicio(i, j));
                    }
                }
            }
        }
        return clausPendents;
    }

    /**
     * Calcula la distància de Manhattan entre dues posicions
     * @param p1 primera posició
     * @param p2 segona posició
     * @return la distància de Manhattan
     */
    public static int distanciaManhattan(Posicio p1, Posicio p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }
}
