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

package org.augur.sense.web.resources;

import org.augur.sense.api.resources.PersonCoordinate;

import java.util.List;

public class ReIDStatus {
    private List<PersonCoordinate> results;

    private boolean isPending;
    private boolean isCompleted;

    private boolean isInvalid;

    public boolean isInvalid() {
        return isInvalid;
    }

    public void setInvalid(boolean invalid) {
        isInvalid = invalid;
    }
    public ReIDStatus(List<PersonCoordinate> results, boolean isPending, boolean isCompleted, boolean isInvalid)
    {
        this.results = results;
        this.isCompleted = isCompleted;
        this.isPending = isPending;
        this.isInvalid = isInvalid;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public List<PersonCoordinate> getResults() {
        return results;
    }

    public void setResults(List<PersonCoordinate> results) {
        this.results = results;
    }
}
