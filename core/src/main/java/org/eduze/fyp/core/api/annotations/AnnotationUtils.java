/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.eduze.fyp.core.api.annotations;

import org.eduze.fyp.core.api.config.Startable;
import org.springframework.context.ApplicationContext;

import java.util.Comparator;

import static org.springframework.core.annotation.AnnotationUtils.*;

/**
 * Utility class to process annotations and act accordingly
 *
 * @author Imesha Sudasingha
 */
public abstract class AnnotationUtils {

    private AnnotationUtils() {
    }

    /**
     * Start all the elements which have annotated with {@link AutoStart} and have implemented {@link Startable}
     *
     * @param context spring application context
     */
    public static void startAnnotatedElements(ApplicationContext context) {
        context.getBeansOfType(Startable.class)
                .values().stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(AutoStart.class))
                .sorted(Comparator.comparingInt(i -> getAnnotation(i.getClass(), AutoStart.class).startOrder()))
                .forEach(Startable::start);
    }

    public static void stopAnnotatedElements(ApplicationContext context) {
        context.getBeansOfType(Startable.class)
                .values().stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(AutoStart.class))
                .sorted((s1, s2) -> Integer.compare(
                        getAnnotation(s2.getClass(), AutoStart.class).startOrder(),
                        getAnnotation(s1.getClass(), AutoStart.class).startOrder())
                ).forEach(Startable::stop);
    }
}
