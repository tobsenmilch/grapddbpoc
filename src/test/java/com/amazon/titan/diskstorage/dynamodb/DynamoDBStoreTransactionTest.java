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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.amazon.titan.diskstorage.dynamodb.DynamoDBStoreTransaction;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTxConfig;

/**
 *
 * @author Matthew Sowders
 * @author Alexander Patrikalakis
 */
public class DynamoDBStoreTransactionTest {
    private DynamoDBStoreTransaction instance;
    private StoreTxConfig config;

    @Before
    public void setup() {
        config = new StoreTxConfig();
        instance = new DynamoDBStoreTransaction(config, "test".getBytes());
    }

    @Test
    public void testEquals() {
        assertFalse(instance.equals(null));
        assertTrue(instance.equals(instance));
    }

}
