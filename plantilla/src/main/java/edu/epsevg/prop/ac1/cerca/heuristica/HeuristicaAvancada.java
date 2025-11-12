package edu.epsevg.prop.ac1.cerca.heuristica;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;

/**
 * Heurística avançada per A*.
 * Estima el cost restant com:
 * - Distància a la clau més propera (si n'hi ha).
 * - Més una estimació del cost per recollir la resta de claus (Nearest Neighbor Greedy).
 * - Més la distància des de l'última clau recollida a la sortida.
 * Calcula totes les distàncies amb Manhattan (ignorant obstacles).
 */
public class HeuristicaAvancada implements Heuristica {

    @Override
    public int h(Mapa estat) {
        // Si ja hem arribat a la meta, h = 0
        if (estat.esMeta()) {
            return 0;
        }

        List<Posicio> agents = estat.getAgents();
        List<Posicio> clausPendents = estat.getClausPendents();
        Posicio sortida = estat.getSortidaPosicio();

        // Si no queden claus pendents, retornar distància mínima a la sortida
        if (clausPendents.isEmpty()) {
            int minDistToExit = Integer.MAX_VALUE;
            for (Posicio agent : agents) {
                int dist = distanciaManhattan(agent, sortida);
                minDistToExit = Math.min(minDistToExit, dist);
            }
            return minDistToExit;
        }

        // Si hi ha claus pendents:

        // 1. Trobar la distància mínima des de qualsevol agent a qualsevol clau
        int minDistToFirstKey = Integer.MAX_VALUE;
        for (Posicio agent : agents) {
            for (Posicio clau : clausPendents) {
                int dist = distanciaManhattan(agent, clau);
                minDistToFirstKey = Math.min(minDistToFirstKey, dist);
            }
        }

        // 2. Estimar el cost per recollir la resta de claus (Nearest Neighbor Greedy)
        // Aquesta és una estimació greedy, no l'òptima, però és ràpida i més informada.
        int estimatedCostToCollectRest = 0;
        if (clausPendents.size() > 1) {
            // Fem una còpia per no alterar la llista original
            List<Posicio> clausRestants = new ArrayList<>(clausPendents);
            // Triem un punt de partida (pot ser qualsevol clau, per exemple, la més propera a un agent)
            Posicio currentKey = clausRestants.stream()
                    .min(Comparator.comparingInt(pos -> minDistToAgent(pos, agents)))
                    .orElse(clausRestants.get(0));

            clausRestants.remove(currentKey); // Comencem des d'aquesta clau

            // Use AtomicReference to allow the variable to be updated inside the loop
            AtomicReference<Posicio> currentRef = new AtomicReference<>(currentKey);

            while (!clausRestants.isEmpty()) {
                Posicio closest = clausRestants.stream()
                        .min(Comparator.comparingInt(pos -> distanciaManhattan(currentRef.get(), pos)))
                        .orElse(clausRestants.get(0));
                estimatedCostToCollectRest += distanciaManhattan(currentRef.get(), closest);
                currentRef.set(closest);
                clausRestants.remove(closest);
            }
        }

        // 3. Estimar la distància des de l'última clau recollida a la sortida
        // Això depèn de quina clau sigui l'última. Per simplificar, usem la distància
        // des de la clau "més allunyada" (segons el camí greedy) a la sortida.
        // Alternativament, podem usar la distància des de la clau més propera a la sortida.
        // Per simplificar i ser més informats, usem la distància mínima de qualsevol clau a la sortida.
        int minDistFromAnyKeyToExit = clausPendents.stream()
                .mapToInt(clau -> distanciaManhattan(clau, sortida))
                .min()
                .orElse(Integer.MAX_VALUE);

        // Heurística total = dist. a primera clau + dist. estimada per recollir la resta + dist. des de la darrera clau a la sortida
        return minDistToFirstKey + estimatedCostToCollectRest + minDistFromAnyKeyToExit;
    }

    /**
     * Troba la distància Manhattan més curta des d'una posició a qualsevol dels agents.
     */
    private int minDistToAgent(Posicio pos, List<Posicio> agents) {
        return agents.stream()
                .mapToInt(agent -> distanciaManhattan(pos, agent))
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    public static int distanciaManhattan(Posicio p1, Posicio p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }
}
