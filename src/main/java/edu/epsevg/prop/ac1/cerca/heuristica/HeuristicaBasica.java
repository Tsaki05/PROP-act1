package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;

import java.util.List;

/** 
 * Distància de Manhattan a la clau més propera 
 * (si queden per recollir) o a la sortida.
 */
public class HeuristicaBasica implements Heuristica {
    
    @Override
    public int h(Mapa estat) {
        // Si ja hem arribat a la meta, h = 0
        if (estat.esMeta()) {
            return 0;
        }
        
        List<Posicio> agents = estat.getAgents();
        List<Posicio> clausPendents = estat.getClausPendents();
        
        // Si hi ha claus pendents, retornar distància a la clau més propera
        if (!clausPendents.isEmpty()) {
            int minDist = Integer.MAX_VALUE;
            
            // Trobar la distància mínima des de qualsevol agent a qualsevol clau
            for (Posicio agent : agents) {
                for (Posicio clau : clausPendents) {
                    int dist = distanciaManhattan(agent, clau);
                    minDist = Math.min(minDist, dist);
                }
            }
            
            return minDist;
        } else {
            // Totes les claus recollides, retornar distància a la sortida
            Posicio sortida = estat.getSortidaPosicio();
            int minDist = Integer.MAX_VALUE;
            
            // Trobar la distància mínima des de qualsevol agent a la sortida
            for (Posicio agent : agents) {
                int dist = distanciaManhattan(agent, sortida);
                minDist = Math.min(minDist, dist);
            }
            
            return minDist;
        }
    }
    
    public static int distanciaManhattan(Posicio p1, Posicio p2) {
    return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
}
}