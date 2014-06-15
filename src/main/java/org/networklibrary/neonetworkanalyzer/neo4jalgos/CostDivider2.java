package org.networklibrary.neonetworkanalyzer.neo4jalgos;

public interface CostDivider2<CostType>
{
    /**
     * @return c / d
     */
    Double divideCost( CostType c, Double d );

    /**
     * @return d / c
     */
    Double divideByCost( Double d, CostType c );
}
