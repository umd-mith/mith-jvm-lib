/*
 * #%L
 * MITH TEI Maven Plugin
 * %%
 * Copyright (C) 2011 Maryland Institute for Technology in the Humanities
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.umd.mith.util.tei;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;

/**
 * The OddSchemaMojo is used for transforming an ODD file into schemas and
 * documentation.
 *
 * @goal odd-schema
 * @phase generate-resources
 */
public class OddSchemaMojo extends OddSchemaGoal
{
  /**
   * Specifies the ODD file to be transformed.
   * @parameter
   */
  private File source;

  /**
   * Specifies the output directory for all schema.
   * @parameter
   */
  private File outputDir;

  /**
   * Specifies the output directory for RelaxNG schema.
   * @parameter
   */
  private File rngOutputDir;

  /**
   * Specifies the output directory for Schematron schema.
   * @parameter
   */
  private File schOutputDir;

  /**
   * Whether creating the transformed files should be forced.
   * @parameter expression="${xml.forceCreation}" default-value="false"
   */
  private boolean forceCreation;

  /**
   * Transformer factory use. By default, the systems default transformer
   * factory is used.
   * <b>If you use this feature you must use at least jdk 1.6</b>
   * @parameter expression="${xml.transformerFactory}"
   */
  private String transformerFactory;

  public File getOdd() {
    return this.source;
  }

  public File getOutputDir() {
    return this.outputDir;
  }

  public File getRngOutputDir() {
    return this.rngOutputDir == null ? super.getRngOutputDir() : this.rngOutputDir;
  }

  public File getSchOutputDir() {
    return this.schOutputDir == null ? super.getSchOutputDir() : this.schOutputDir;
  }

  public boolean getForceCreation() {
    return this.forceCreation;
  }

  public String getTransformerFactoryClassName() {
    return this.transformerFactory;
  }
}

