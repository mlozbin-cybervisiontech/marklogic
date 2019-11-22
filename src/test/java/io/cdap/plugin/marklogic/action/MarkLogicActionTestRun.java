/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.marklogic.action;

import com.google.common.collect.ImmutableMap;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.TextDocumentManager;
import io.cdap.cdap.etl.api.action.Action;
import io.cdap.cdap.etl.mock.batch.MockSink;
import io.cdap.cdap.etl.mock.batch.MockSource;
import io.cdap.cdap.etl.proto.v2.ETLBatchConfig;
import io.cdap.cdap.etl.proto.v2.ETLPlugin;
import io.cdap.cdap.etl.proto.v2.ETLStage;
import io.cdap.cdap.proto.artifact.AppRequest;
import io.cdap.cdap.proto.id.ApplicationId;
import io.cdap.cdap.proto.id.NamespaceId;
import io.cdap.cdap.test.ApplicationManager;
import io.cdap.plugin.marklogic.BaseMarkLogicTest;
import io.cdap.plugin.marklogic.MarkLogicPluginConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link MarkLogicAction}.
 */
public class MarkLogicActionTestRun extends BaseMarkLogicTest {
  private static final String DELETE_QUERY = String.format("xquery version \"1.0-ml\";\nxdmp:document-delete(\"%s\")",
                                                           TEXT_DOCUMENT);

  @Test
  public void testAction() throws Exception {
    ETLStage source = new ETLStage("source", MockSource.getPlugin("actionInput"));
    ETLStage sink = new ETLStage("sink", MockSink.getPlugin("actionOutput"));
    ETLStage action = new ETLStage("action", new ETLPlugin(
      MarkLogicPluginConstants.PLUGIN_NAME,
      Action.PLUGIN_TYPE,
      ImmutableMap.<String, String>builder()
        .putAll(BASE_PROPS)
        .put(MarkLogicActionConfig.QUERY, DELETE_QUERY)
        .build(),
      null));

    ETLBatchConfig config = ETLBatchConfig.builder()
      .addStage(source)
      .addStage(sink)
      .addStage(action)
      .addConnection(sink.getName(), action.getName())
      .addConnection(source.getName(), sink.getName())
      .build();

    AppRequest<ETLBatchConfig> appRequest = new AppRequest<>(DATAPIPELINE_ARTIFACT, config);
    ApplicationId appId = NamespaceId.DEFAULT.app("actionTest");
    ApplicationManager appManager = deployApplication(appId, appRequest);
    runETLOnce(appManager, ImmutableMap.of("logical.start.time", "0"));

    DatabaseClient client = getDatabaseClient();
    DocumentDescriptor descriptor;
    try {
      TextDocumentManager docMgr = client.newTextDocumentManager();
      descriptor = docMgr.exists(TEXT_DOCUMENT);
    } finally {
      client.release();
    }

    Assert.assertNull(descriptor);
  }
}