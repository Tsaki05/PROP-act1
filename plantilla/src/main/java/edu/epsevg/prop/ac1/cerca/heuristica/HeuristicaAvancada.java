package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;

import java.util.*;

/**
 * Heuristica avançada: Al vostre gust ;-)
 */
public class HeuristicaAvancada implements Heuristica {
    
    @Override
    public int h(Mapa estat) {
        
        
        //@TODO: reemplaceu tot el codi per la vostra heurística.
        
        if(estat.esMeta()) 
            return 0;
        else 
            return 1;
    }

     
}
