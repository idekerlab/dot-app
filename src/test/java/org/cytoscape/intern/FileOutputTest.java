/**************************
 * Copyright © 2015-2017 Braxton Fitts, Ziran Zhang, Massoud Maher
 * 
 * This file is part of dot-app.
 * dot-app is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * dot-app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with dot-app.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cytoscape.intern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Ignore;
import org.junit.Test;

public class FileOutputTest {

	@Ignore("Used for testing after a file is made")
	@Test
	public void testOutput() {
		InputStream expectedFile = ClassLoader.getSystemResourceAsStream("expected_dot_files/karate.gv");
		InputStream actualFile = ClassLoader.getSystemResourceAsStream("actual_dot_files/karate.gv");
		BufferedReader expectedReader = new BufferedReader(new InputStreamReader(expectedFile));
		BufferedReader actualReader = new BufferedReader(new InputStreamReader(actualFile));
		String expectedRead;
		String actualRead;
		try {
			while ((expectedRead = expectedReader.readLine()) != null && ( actualRead = actualReader.readLine()) != null) {
				assertEquals("Actual output didn't match expected", expectedRead, actualRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
