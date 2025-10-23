package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;


/** 
 * Distància de Manhattan a la clau més propera 
 * (si queden per recollir) o a la sortida.
 */
public class HeuristicaBasica implements Heuristica {
    @Override
    public int h(Mapa estat) {
        
        
        //@TODO: reemplaceu tot el codi per la vostra heurística.
        
        if(estat.esMeta()) 
            return 0;
        else 
            return 1;
    }
}
