/*
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.titan.diskstorage.dynamodb;

import java.util.Collection;
import java.util.Map;

import com.amazon.titan.diskstorage.dynamodb.mutation.MutateWorker;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KCVMutation;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyColumnValueStore;

/**
 * Responsible for communicating with a single AWS backing store table.
 *
 * @author Matthew Sowders
 * @author Alexander Patrikalakis
 *
 */
public interface AwsStore extends KeyColumnValueStore {

    /**
     * Creates the KCV store and underlying DynamoDB table.
     * @throws StorageException
     */
    void ensureStore() throws StorageException;

    /**
     * Deletes the KCV store and underlying DynamoDB table.
     * @throws StorageException
     */
    void deleteStore() throws StorageException;

    /**
     * Titan relies on static store names to be used, but we want the ability to
     * have multiple graphs in a single region, so prepend a configurable prefix to the
     * underlying table names of each graph and get the DynamoDB table name with this method
     *
     * @return
     */
    String getTableName();

    /**
     * Creates workers whose job it is to commit the actual mutations in the given mutation map.
     * @param mutationMap
     * @param txh
     * @return a collection of MutateWorker objects that when executed will commit all changes specified by mutationMap
     */
    Collection<MutateWorker> createMutationWorkers(Map<StaticBuffer, KCVMutation> mutationMap,
                                                   DynamoDBStoreTransaction txh);
}
