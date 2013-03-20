/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.wl.events.WLPublishedRepositoryEvent;
import org.sonatype.nexus.proxy.maven.wl.events.WLUnpublishedRepositoryEvent;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

/**
 * Testing issues NEXUS-5602 and NEXUS-5608
 * 
 * @author cstamas
 */
public class WLUpdatePropagationGroupUpdatesTest
    extends AbstractWLProxyTest
{
    private static final String HOSTED1_REPO_ID = "hosted1";

    private static final String HOSTED2_REPO_ID = "hosted2";

    private static final String GROUP_REPO_ID = "group";

    private final WLUpdateListener wlUpdateListener;

    public WLUpdatePropagationGroupUpdatesTest()
        throws Exception
    {
        this.wlUpdateListener = new WLUpdateListener();
    }

    protected static class WLUpdateListener
    {
        private final List<String> publishedIds;

        private final List<String> unpublishedIds;

        public WLUpdateListener()
        {
            this.publishedIds = new ArrayList<String>();
            this.unpublishedIds = new ArrayList<String>();
            reset();
        }

        @Subscribe
        public void on( final WLPublishedRepositoryEvent evt )
        {
            publishedIds.add( evt.getRepository().getId() );
        }

        @Subscribe
        public void on( final WLUnpublishedRepositoryEvent evt )
        {
            unpublishedIds.add( evt.getRepository().getId() );
        }

        public List<String> getPublished()
        {
            return publishedIds;
        }

        public List<String> getUnpublished()
        {
            return unpublishedIds;
        }

        public void reset()
        {
            publishedIds.clear();
            unpublishedIds.clear();
        }
    }

    protected File getStorageRoot( final String repoId )
    {
        return getApplicationConfiguration().getWorkingDirectory( "proxy/store/" + repoId );
    }

    @Override
    protected EnvironmentBuilder createEnvironmentBuilder()
        throws Exception
    {
        // we need one hosted repo only, so build it
        return new EnvironmentBuilder()
        {
            @Override
            public void startService()
            {
            }

            @Override
            public void stopService()
            {
            }

            @Override
            public void buildEnvironment( AbstractProxyTestEnvironment env )
                throws ConfigurationException, IOException, ComponentLookupException
            {
                final PlexusContainer container = env.getPlexusContainer();
                final List<String> reposes = new ArrayList<String>();
                {
                    // adding one hosted
                    final M2Repository repo = (M2Repository) container.lookup( Repository.class, "maven2" );
                    CRepository repoConf = new DefaultCRepository();
                    repoConf.setProviderRole( Repository.class.getName() );
                    repoConf.setProviderHint( "maven2" );
                    repoConf.setId( HOSTED1_REPO_ID );
                    repoConf.setName( HOSTED1_REPO_ID );
                    repoConf.setLocalStorage( new CLocalStorage() );
                    repoConf.getLocalStorage().setProvider( "file" );
                    repoConf.getLocalStorage().setUrl(
                        env.getApplicationConfiguration().getWorkingDirectory( "proxy/store/" + HOSTED1_REPO_ID ).toURI().toURL().toString() );
                    Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
                    repoConf.setExternalConfiguration( exRepo );
                    M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
                    exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
                    exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );
                    repo.configure( repoConf );
                    reposes.add( repo.getId() );
                    env.getApplicationConfiguration().getConfigurationModel().addRepository( repoConf );
                    env.getRepositoryRegistry().addRepository( repo );
                }
                {
                    // adding one hosted
                    final M2Repository repo = (M2Repository) container.lookup( Repository.class, "maven2" );
                    CRepository repoConf = new DefaultCRepository();
                    repoConf.setProviderRole( Repository.class.getName() );
                    repoConf.setProviderHint( "maven2" );
                    repoConf.setId( HOSTED2_REPO_ID );
                    repoConf.setName( HOSTED2_REPO_ID );
                    repoConf.setLocalStorage( new CLocalStorage() );
                    repoConf.getLocalStorage().setProvider( "file" );
                    repoConf.getLocalStorage().setUrl(
                        env.getApplicationConfiguration().getWorkingDirectory( "proxy/store/" + HOSTED2_REPO_ID ).toURI().toURL().toString() );
                    Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
                    repoConf.setExternalConfiguration( exRepo );
                    M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
                    exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
                    exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );
                    repo.configure( repoConf );
                    reposes.add( repo.getId() );
                    env.getApplicationConfiguration().getConfigurationModel().addRepository( repoConf );
                    env.getRepositoryRegistry().addRepository( repo );
                }
                {
                    // add a group
                    final M2GroupRepository group =
                        (M2GroupRepository) container.lookup( GroupRepository.class, "maven2" );
                    CRepository repoGroupConf = new DefaultCRepository();
                    repoGroupConf.setProviderRole( GroupRepository.class.getName() );
                    repoGroupConf.setProviderHint( "maven2" );
                    repoGroupConf.setId( GROUP_REPO_ID );
                    repoGroupConf.setName( GROUP_REPO_ID );
                    repoGroupConf.setLocalStorage( new CLocalStorage() );
                    repoGroupConf.getLocalStorage().setProvider( "file" );
                    repoGroupConf.getLocalStorage().setUrl(
                        env.getApplicationConfiguration().getWorkingDirectory( "proxy/store/test" ).toURI().toURL().toString() );
                    Xpp3Dom exGroupRepo = new Xpp3Dom( "externalConfiguration" );
                    repoGroupConf.setExternalConfiguration( exGroupRepo );
                    M2GroupRepositoryConfiguration exGroupRepoConf = new M2GroupRepositoryConfiguration( exGroupRepo );
                    exGroupRepoConf.setMemberRepositoryIds( reposes );
                    exGroupRepoConf.setMergeMetadata( true );
                    group.configure( repoGroupConf );
                    env.getApplicationConfiguration().getConfigurationModel().addRepository( repoGroupConf );
                    env.getRepositoryRegistry().addRepository( group );
                }

                // register it here BEFORE boot process starts but plx is already created
                container.lookup( EventBus.class ).register( wlUpdateListener );
            }
        };
    }

    @Override
    protected boolean enableWLFeature()
    {
        return true;
    }

    @Test
    public void testUpdateCountOnBootWithoutWL()
    {
        // boot already happened
        // we have 2 hosted and both are members of the group
        // as we have no WL (clean boot/kinda upgrade), all of them was 1st marked for noscrape,
        // and then H1 and H2 got WL updated concurrently in bg job, and as side effect group WL got updated too
        // This means, that group might be updated once or twice, depending on concurrency.
        // So the list might contain (in any order):
        // HOSTED1, HOSTED2, GROUP
        // or
        // HOSTED1, HOSTED2, GROUP, GROUP
        // (group two times updated).
        assertThat( wlUpdateListener.getPublished(), hasItem( HOSTED1_REPO_ID ) );
        assertThat( wlUpdateListener.getPublished(), hasItem( HOSTED2_REPO_ID ) );
        assertThat( wlUpdateListener.getPublished(), hasItem( GROUP_REPO_ID ) );
    }

    @Test
    public void testUpdateCountOnGroupMemberChange()
        throws Exception
    {
        wlUpdateListener.reset();

        final MavenGroupRepository mgr =
            getRepositoryRegistry().getRepositoryWithFacet( GROUP_REPO_ID, MavenGroupRepository.class );

        mgr.removeMemberRepositoryId( HOSTED1_REPO_ID );
        getApplicationConfiguration().saveConfiguration();

        assertThat( wlUpdateListener.getPublished(), contains( GROUP_REPO_ID ) );

        mgr.addMemberRepositoryId( HOSTED1_REPO_ID );
        getApplicationConfiguration().saveConfiguration();

        assertThat( wlUpdateListener.getPublished(), contains( GROUP_REPO_ID, GROUP_REPO_ID ) );
    }
}
