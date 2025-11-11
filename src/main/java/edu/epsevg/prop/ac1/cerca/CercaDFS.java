package edu.epsevg.prop.ac1.cerca;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Moviment;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

public class CercaDFS extends Cerca {
    
    private static final int MAX_DEPTH = 50; // Límit de profunditat
    
    public CercaDFS(boolean usarLNT) { 
        super(usarLNT); 
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        // Stack per DFS (LIFO)
        Deque<Node> stack = new ArrayDeque<>();
        Map<Mapa, Integer> LNT = usarLNT ? new HashMap<>() : null;
        
        Node initialNode = new Node(inicial, null, null, 0, 0);
        stack.push(initialNode);
        
        if (usarLNT) {
            LNT.put(inicial, 0);
        }
        
        while (!stack.isEmpty()) {
            Node current = stack.pop();
            rc.incNodesExplorats();
            
            // Actualitzar memòria
            int memoria = stack.size();
            if (usarLNT) memoria += LNT.size();
            rc.updateMemoria(memoria);
            
            // Comprovar meta
            if (current.estat.esMeta()) {
                rc.setCami(reconstruirCami(current));
                return;
            }
            
            // No expandir si ja hem arribat al límit
            if (current.depth >= MAX_DEPTH) {
                continue;
            }
            
            // Expandir
            List<Moviment> accions = current.estat.getAccionsPossibles();
            
            // Processar en ordre invers per mantenir ordre DFS correcte
            for (int i = accions.size() - 1; i >= 0; i--) {
                Moviment accio = accions.get(i);
                Mapa nouEstat = current.estat.mou(accio);
                int nouDepth = current.depth + 1;
                
                boolean repetit = false;
                
                if (usarLNT) {
                    if (LNT.containsKey(nouEstat)) {
                        if (LNT.get(nouEstat) <= nouDepth) {
                            repetit = true;
                        } else {
                            LNT.put(nouEstat, nouDepth);
                        }
                    } else {
                        LNT.put(nouEstat, nouDepth);
                    }
                } else {
                    repetit = estaDinsDelCami(current, nouEstat);
                }
                
                if (repetit) {
                    rc.incNodesTallats();
                } else {
                    stack.push(new Node(nouEstat, current, accio, nouDepth, 0));
                }
            }
        }
        
        // No trobat
        rc.setCami(null);
    }
    
    private List<Moviment> reconstruirCami(Node node) {
        List<Moviment> cami = new ArrayList<>();
        while (node.pare != null) {
            cami.add(0, node.accio);
            node = node.pare;
        }
        return cami;
    }
    
    private boolean estaDinsDelCami(Node node, Mapa estat) {
        while (node != null) {
            if (node.estat.equals(estat)) return true;
            node = node.pare;
        }
        return false;
    }
}
