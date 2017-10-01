/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package org.eduze.fyp;

import org.eduze.fyp.impl.db.model.Person;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.Date;
import java.util.List;

public class AnalyticsControllerTest extends AbstractTestCase {

    @Test
    public void testGetHeatMap() {
        Date start = new Date(1451606400);
        Date end = new Date();
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .host("localhost")
                .port(8085)
                .path("v1")
                .path("analytics")
                .path("heatMap")
                .path(String.valueOf(start.getTime()))
                .path(String.valueOf(end.getTime()));

        List<Person> people = client.target(builder)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Person>>() {});

        Assert.assertNotNull(people);
    }
}
