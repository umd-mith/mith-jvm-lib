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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceLoader;

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
   * Plexus resource manager used to obtain XSL.
   * 
   * @component
   * @required
   * @readonly
   */
  private ResourceManager locator;

  private boolean locatorInitialized = false;

  protected ResourceManager getLocator() {
    if (!this.locatorInitialized) {
      this.locator.addSearchPath(FileResourceLoader.ID, this.getBasedir().getAbsolutePath());
      this.locatorInitialized = true;
    }
    return this.locator;
  }

  /**
   * The base directory, relative to which directory names are
   * interpreted.
   *
   * @parameter expression="${basedir}"
   * @required
   * @readonly
   */
  private File basedir;

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

  protected File getBasedir() {
    return basedir;
  }

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

