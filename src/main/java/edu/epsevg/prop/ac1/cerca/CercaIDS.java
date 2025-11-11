package edu.epsevg.prop.ac1.cerca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Moviment;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

public class CercaIDS extends Cerca {
    
    private Map<Mapa, Integer> LNT;
    private ResultatCerca rc;
    private int limitActual;
    
    public CercaIDS(boolean usarLNT) { 
        super(usarLNT); 
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        this.rc = rc;
        
        // Iterative Deepening: incrementem el límit de profunditat
        for (int limit = 0; limit < Integer.MAX_VALUE; limit++) {
            this.limitActual = limit;
            this.LNT = usarLNT ? new HashMap<>() : null;
            
            Node nodeInicial = new Node(inicial, null, null, 0, 0);
            
            if (usarLNT) {
                LNT.put(inicial, 0);
            }
            
            List<Moviment> solucio = cercaLimitada(nodeInicial, 0);
            
            if (solucio != null) {
                rc.setCami(solucio);
                return;
            }
            
            // Si estem usant LNT, netejar-la per la següent iteració
            // (cada iteració és independent en IDS)
        }
        
        // No s'ha trobat solució
        rc.setCami(null);
    }
    
    /**
     * Cerca en profunditat limitada
     */
    private List<Moviment> cercaLimitada(Node actual, int profunditatFrontera) {
        rc.incNodesExplorats();
        
        // Actualitzar memòria pic
        int memoriaActual = profunditatFrontera;
        if (usarLNT) {
            memoriaActual += LNT.size();
        }
        rc.updateMemoria(memoriaActual);
        
        // Comprovar si és meta
        if (actual.estat.esMeta()) {
            return new ArrayList<>();
        }
        
        // Si hem arribat al límit de profunditat, no expandir
        if (actual.depth >= limitActual) {
            return null;
        }
        
        // Expandir node
        List<Moviment> accions = actual.estat.getAccionsPossibles();
        
        for (Moviment accio : accions) {
            Mapa nouEstat = actual.estat.mou(accio);
            int novaDepth = actual.depth + 1;
            
            boolean esRepetit = false;
            
            if (usarLNT) {
                // Control amb LNT
                if (LNT.containsKey(nouEstat)) {
                    int depthAnterior = LNT.get(nouEstat);
                    if (depthAnterior <= novaDepth) {
                        esRepetit = true;
                    } else {
                        LNT.put(nouEstat, novaDepth);
                    }
                } else {
                    LNT.put(nouEstat, novaDepth);
                }
            } else {
                // Control dins de la branca actual
                esRepetit = estaDinsDelCami(actual, nouEstat);
            }
            
            if (esRepetit) {
                rc.incNodesTallats();
            } else {
                Node nouNode = new Node(nouEstat, actual, accio, novaDepth, 0);
                
                List<Moviment> cami = cercaLimitada(nouNode, profunditatFrontera + 1);
                
                if (cami != null) {
                    cami.add(0, accio);
                    return cami;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Comprova si un estat està repetit dins del camí actual (branca)
     */
    private boolean estaDinsDelCami(Node node, Mapa estat) {
        Node actual = node;
        while (actual != null) {
            if (actual.estat.equals(estat)) {
                return true;
            }
            actual = actual.pare;
        }
        return false;
    }
}
