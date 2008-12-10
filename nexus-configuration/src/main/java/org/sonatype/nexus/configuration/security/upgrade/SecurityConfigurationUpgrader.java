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
package org.sonatype.nexus.configuration.security.upgrade;

import java.io.File;
import java.io.IOException;

import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UnsupportedConfigurationVersionException;

/**
 * A component involved only if old Nexus configuration is found. It will fetch the old configuration, transform it to
 * current Configuration model and return it. Nothing else.
 * 
 * @author cstamas
 */
public interface SecurityConfigurationUpgrader
{
    /**
     * Tries to load an old configuration from file and will try to upgrade it to current model.
     * 
     * @param file
     * @return
     * @throws IOException
     * @throws ConfigurationIsCorruptedException
     * @throws UnsupportedConfigurationVersionException
     */
    public Configuration loadOldConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException,
            UnsupportedConfigurationVersionException;
}
