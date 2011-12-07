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
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The system settings for Maven. This is the instance resulting from 
     * merging global- and user-level settings files.
     * 
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * Plexus resource manager used to obtain XSL.
     * 
     * @component
     * @required
     * @readonly
     */
    private ResourceManager locator;

    private boolean locatorInitialized;

    /**
     * The base directory, relative to which directory names are
     * interpreted.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /** An XML catalog file, or URL, which is being used to resolve
     * entities.
     * @parameter
     */
    private String[] catalogs;

    /**
     * Returns the maven project.
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Returns the projects base directory.
     */
    protected File getBasedir()
    {
        return basedir;
    }

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

    protected ResourceManager getLocator()
    {
		if ( !locatorInitialized )
    	{
        	locator.addSearchPath( FileResourceLoader.ID, getBasedir().getAbsolutePath() );
    		locatorInitialized = true;
    	}
		return locator;
	}

    /**
     * Returns the plugins catalog files.
     */
    protected void setCatalogs( List pCatalogFiles, List pCatalogUrls )
    {
        if ( catalogs == null  ||  catalogs.length == 0 )
        {
            return;
        }

        for ( int i = 0; i < catalogs.length; i++ )
        {
        	try
        	{
        		URL url = new URL( catalogs[i] );
        		pCatalogUrls.add( url );
        	}
        	catch ( MalformedURLException e )
        	{
                pCatalogFiles.add( asAbsoluteFile( new File( catalogs[i] ) ) );
        	}
        }
    }

    private boolean isEmpty( String value )
    {
        return value == null  ||  value.trim().length() == 0;
    }

    private void setProperty( List pProperties, String pKey, String pValue )
    {
        if ( pProperties != null )
        {
            pProperties.add( pKey );
            pProperties.add( System.getProperty( pKey ) );
        }
        if ( pValue == null )
        {
            System.getProperties().remove( pKey );
        }
        else
        {
            System.setProperty( pKey, pValue );
        }
    }

    /**
     * Called to install the plugins proxy settings.
     */
    protected Object activateProxy()
    {
        if ( settings == null )
        {
            return null;
        }
        final Proxy proxy = settings.getActiveProxy();
        if ( proxy == null )
        {
            return null;
        }

        final List properties = new ArrayList();
        final String protocol = proxy.getProtocol();
        final String prefix = isEmpty( protocol ) ? "" : ( protocol + "." );

        final String host = proxy.getHost();
        final String hostProperty = prefix + "proxyHost";
        final String hostValue = isEmpty( host ) ? null : host;
        setProperty( properties, hostProperty, hostValue );
        final int port = proxy.getPort();
        final String portProperty = prefix + "proxyPort";
        final String portValue = ( port == 0 || port == -1 ) ? null : String.valueOf( port );
        setProperty( properties, portProperty, portValue );
        final String username = proxy.getUsername();
        final String userProperty = prefix + "proxyUser";
        final String userValue = isEmpty( username ) ? null : username;
        setProperty( properties, userProperty, userValue );
        final String password = proxy.getPassword();
        final String passwordProperty = prefix + "proxyPassword";
        final String passwordValue = isEmpty( password ) ? null : password;
        setProperty( properties, passwordProperty, passwordValue );
        final String nonProxyHosts = proxy.getNonProxyHosts();
        final String nonProxyHostsProperty = prefix + "nonProxyHosts";
        final String nonProxyHostsValue = isEmpty( nonProxyHosts ) ? null : nonProxyHosts.replace( ',' , '|' );
        setProperty( properties, nonProxyHostsProperty, nonProxyHostsValue );
        getLog().debug( "Proxy settings: " + hostProperty + "=" + hostValue
                       + ", " + portProperty + "=" + portValue
                       + ", " + userProperty + "=" + userValue
                       + ", " + passwordProperty + "=" + (passwordValue == null ? "null" : "<PasswordNotLogged>")
                       + ", " + nonProxyHostsProperty + "=" + nonProxyHostsValue );
        return properties;
    }
}

