/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright � 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.restore;

import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.scheduling.NexusTask;

public abstract class AbstractRestorePlexusResource
    extends AbstractNexusPlexusResource
{

    public static final String DOMAIN = "domain";

    public static final String DOMAIN_REPOSITORIES = "repositories";

    public static final String DOMAIN_REPO_GROUPS = "repo_groups";

    public static final String TARGET_ID = "target";

    public AbstractRestorePlexusResource()
    {
        this.setModifiable( true );
    }

    protected String getRepositoryId( Request request )
    {
        String repoId = null;
        if ( ( request.getAttributes().containsKey( DOMAIN ) && request.getAttributes().containsKey( TARGET_ID ) )
            && DOMAIN_REPOSITORIES.equals( request.getAttributes().get( DOMAIN ) ) )
        {
            repoId = request.getAttributes().get( TARGET_ID ).toString();
        }
        return repoId;
    }

    protected String getRepositoryGroupId( Request request )
    {
        String groupId = null;
        if ( ( request.getAttributes().containsKey( DOMAIN ) && request.getAttributes().containsKey( TARGET_ID ) )
            && DOMAIN_REPO_GROUPS.equals( request.getAttributes().get( DOMAIN ) ) )
        {
            groupId = request.getAttributes().get( TARGET_ID ).toString();
        }
        return groupId;
    }

    protected String getResourceStorePath( Request request )
    {
        String path = null;

        if ( getRepositoryId( request ) != null || getRepositoryGroupId( request ) != null )
        {
            path = request.getResourceRef().getRemainingPart();

            // get rid of query part
            if ( path.contains( "?" ) )
            {
                path = path.substring( 0, path.indexOf( '?' ) );
            }

            // get rid of reference part
            if ( path.contains( "#" ) )
            {
                path = path.substring( 0, path.indexOf( '#' ) );
            }

            if ( StringUtils.isEmpty( path ) )
            {
                path = "/";
            }
        }
        return path;
    }

    public void handleDelete( NexusTask<?> task, Request request )
        throws ResourceException
    {
        try
        {
            // check reposes
            if ( getRepositoryGroupId( request ) != null )
            {
                getNexus().readRepositoryGroup( getRepositoryGroupId( request ) );
            }
            else if ( getRepositoryId( request ) != null )
            {
                try
                {
                    getNexus().readRepository( getRepositoryId( request ) );
                }
                catch ( NoSuchRepositoryException e )
                {
                    getNexus().readRepositoryShadow( getRepositoryId( request ) );
                }
            }

            getNexus().submit( "Internal", task );

            throw new ResourceException( Status.SUCCESS_NO_CONTENT );
        }
        catch ( RejectedExecutionException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, e.getMessage() );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
