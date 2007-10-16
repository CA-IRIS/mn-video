/*
 * Project: Video
 * Copyright (C) 2007  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package us.mn.state.dot.video;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/** The ConnectionFactory is a convenience class for setting 
 * up URLConnections with the appropriate parameters including
 * connection timeout.
 * 
 * @author Timothy A. Johnson
 *
 */
abstract public class ConnectionFactory {

	public static URLConnection createConnection(URL url)
			throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		HttpURLConnection.setFollowRedirects(true);
		c.setConnectTimeout(VideoThread.TIMEOUT_PROXY);
		c.setReadTimeout(VideoThread.TIMEOUT_PROXY);
		return c;
	}
}
