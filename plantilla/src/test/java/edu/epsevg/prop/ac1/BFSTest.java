package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

public class CercaBFS extends Cerca {
    
    public CercaBFS(boolean usarLNT) { 
        super(usarLNT); 
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        Queue<Node> frontera = new LinkedList<>();
        Map<Mapa, Integer> LNT = usarLNT ? new HashMap<>() : null;
        
        // Afegir node inicial
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        frontera.add(nodeInicial);
        
        if (usarLNT) {
            LNT.put(inicial, 0);
        }
        
        while (!frontera.isEmpty()) {
            Node actual = frontera.poll();
            rc.incNodesExplorats();
            
            // Actualitzar memòria pic
            int memoriaActual = frontera.size();
            if (usarLNT) {
                memoriaActual += LNT.size();
            }
            rc.updateMemoria(memoriaActual);
            
            // Comprovar si és meta
            if (actual.estat.esMeta()) {
                rc.setCami(reconstruirCami(actual));
                return;
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
                            // Hem trobat un camí més curt
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
                    frontera.add(nouNode);
                }
            }
        }
        
        // No s'ha trobat solució
        rc.setCami(null);
    }
    
    /**
     * Reconstrueix el camí des del node final fins a l'inicial
     */
    private List<Moviment> reconstruirCami(Node nodeFinal) {
        List<Moviment> cami = new ArrayList<>();
        Node actual = nodeFinal;
        
        while (actual.pare != null) {
            cami.add(0, actual.accio);
            actual = actual.pare;
        }
        
        return cami;
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
