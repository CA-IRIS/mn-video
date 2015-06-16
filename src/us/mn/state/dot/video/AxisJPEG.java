/*
 * VideoServer
 * Copyright (C) 2014-2015  AHMCT, University of California
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
 * Foundation, Inc., 59 temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.video;

import java.net.URL;
import java.util.Properties;

/**
 * The AxisJPEG class represents an Axis encoder that uses a
 * MultiRequestDataSource.
 *
 * @author    Travis Swanston
 */

public final class AxisJPEG extends Axis {

	/** Constructor */
	public AxisJPEG(String host, String user, String pass,
		Properties props)
	{
		super(host, user, pass, props);
	}

	@Override
	public DataSource getDataSource(Client c) throws VideoException{
		URL url = getImageURL(c);
		if (url == null)
			return null;
		try {
			return new JPEGStreamDataSource(c, url, username,
				password, jpeg_period);
		}
		catch(Exception e) {
			throw new VideoException(e.getMessage());
		}
	}

}

