/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

/**
 * SnapshotRemoverTask
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.scheduling.NexusTask"
 *                   role-hint="org.sonatype.nexus.maven.tasks.SnapshotRemoverTask" instantiation-strategy="per-lookup"
 */
public class SnapshotRemoverTask
    extends AbstractNexusRepositoriesTask<SnapshotRemovalResult>
{
    public static final String SYSTEM_REMOVE_SNAPSHOTS_ACTION = "REMOVESNAPSHOTS";

    public static final String MIN_SNAPSHOTS_TO_KEEP_KEY = "minSnapshotsToKeep";

    public static final String REMOVE_OLDER_THAN_DAYS_KEY = "removeOlderThanDays";

    public static final String REMOVE_IF_RELEASE_EXISTS_KEY = "removeIfReleaseExists";

    public int getMinSnapshotsToKeep()
    {
        return Integer.parseInt( getParameters().get( MIN_SNAPSHOTS_TO_KEEP_KEY ) );
    }

    public void setMinSnapshotsToKeep( int minSnapshotsToKeep )
    {
        getParameters().put( MIN_SNAPSHOTS_TO_KEEP_KEY, Integer.toString( minSnapshotsToKeep ) );
    }

    public int getRemoveOlderThanDays()
    {
        return Integer.parseInt( getParameters().get( REMOVE_OLDER_THAN_DAYS_KEY ) );
    }

    public void setRemoveOlderThanDays( int removeOlderThanDays )
    {
        getParameters().put( REMOVE_OLDER_THAN_DAYS_KEY, Integer.toString( removeOlderThanDays ) );
    }

    public boolean isRemoveIfReleaseExists()
    {
        return Boolean.parseBoolean( getParameters().get( REMOVE_IF_RELEASE_EXISTS_KEY ) );
    }

    public void setRemoveIfReleaseExists( boolean removeIfReleaseExists )
    {
        getParameters().put( REMOVE_IF_RELEASE_EXISTS_KEY, Boolean.toString( removeIfReleaseExists ) );
    }

    public SnapshotRemovalResult doRun()
        throws Exception
    {
        SnapshotRemovalRequest req = new SnapshotRemovalRequest(
            getRepositoryId(),
            getRepositoryGroupId(),
            getMinSnapshotsToKeep(),
            getRemoveOlderThanDays(),
            isRemoveIfReleaseExists() );

        return getNexus().removeSnapshots( req );
    }

    protected String getAction()
    {
        return SYSTEM_REMOVE_SNAPSHOTS_ACTION;
    }

    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Removing snapshots from repository group with ID=" + getRepositoryGroupId();
        }
        else if ( getRepositoryId() != null )
        {
            return "Removing snapshots from repository with ID=" + getRepositoryId();
        }
        else
        {
            return "Removing snapshots from all registered repositories";
        }
    }

}
