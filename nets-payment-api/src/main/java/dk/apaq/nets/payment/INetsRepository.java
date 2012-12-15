/*
 * Copyright by Apaq 2011-2013
 */

package dk.apaq.nets.payment;

import dk.apaq.framework.repository.Repository;

/**
 * Javadoc
 */
public interface INetsRepository extends Repository<ITransactionData, String> {

    ITransactionData createNew();
}
