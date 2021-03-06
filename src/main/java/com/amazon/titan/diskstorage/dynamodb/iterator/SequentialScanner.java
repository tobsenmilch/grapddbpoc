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
package com.amazon.titan.diskstorage.dynamodb.iterator;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.amazon.titan.diskstorage.dynamodb.StorageRuntimeException;
import com.amazon.titan.diskstorage.dynamodb.DynamoDBDelegate;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.common.base.Preconditions;

/**
 * Iterator that goes through a scan operation over a given DynamoDB table.
 * If a call to next() returns a result that indicates there are more records to scan, the next ScanRequest
 * is initiated asynchronously.
 *
 * @author Alexander Patrikalakis
 * @author Michael Rodaitis
 */
public class SequentialScanner implements Scanner {

    // This worker defaults to having a next result
    boolean hasNext = true;

    private final DynamoDBDelegate dynamoDBDelegate;
    private final ScanRequest request;
    private int lastConsumedCapacity;
    private Future<ScanResult> currentFuture;

    public SequentialScanner(DynamoDBDelegate dynamoDBDelegate, ScanRequest request) {
        this.dynamoDBDelegate = dynamoDBDelegate;
        Preconditions.checkArgument(request.getExclusiveStartKey() == null || request.getExclusiveStartKey().isEmpty(),
                                    "A scan worker should start with a fresh ScanRequest");
        this.request = DynamoDBDelegate.copyScanRequest(request);
        this.lastConsumedCapacity = dynamoDBDelegate.estimateCapacityUnits(DynamoDBDelegate.SCAN, request.getTableName());
        this.currentFuture = dynamoDBDelegate.scanAsync(request, lastConsumedCapacity);
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public ScanContext next() {
        ScanResult result = null;
        boolean interrupted = false;
        try {
            result = currentFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new StorageRuntimeException(dynamoDBDelegate.unwrapExecutionException(e, DynamoDBDelegate.SCAN));
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        // Copy the request used to get this ScanResult to create the proper ScanContext later
        final ScanRequest requestForResult = DynamoDBDelegate.copyScanRequest(this.request);

        if (result.getConsumedCapacity() != null) {
            lastConsumedCapacity = result.getConsumedCapacity().getCapacityUnits().intValue();
        }

        if (result.getLastEvaluatedKey() != null && !result.getLastEvaluatedKey().isEmpty()) {
            hasNext = true;
            request.setExclusiveStartKey(result.getLastEvaluatedKey());
            currentFuture = dynamoDBDelegate.scanAsync(request, lastConsumedCapacity);
        } else {
            hasNext = false;
        }
        return new ScanContext(requestForResult, result);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public void close() throws IOException {
        currentFuture.cancel(true);
    }
}
