/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mp4parser.support;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
/**
 * An aspect to trigger the actual parsing of boxes' content when it is actually needed. This aspect
 * calls {@link AbstractBox#parseDetails()} before actually executing
 * the method.
 */
public class RequiresParseDetailAspect {


    @Before("this(org.mp4parser.support.AbstractBox) && ((execution(public * * (..)) && !( " +
            "execution(* parseDetails()) || " +
            "execution(* getNumOfBytesToFirstChild()) || " +
            "execution(* getType()) || " +
            "execution(* isParsed()) || " +
            "execution(* getHeader(*)) || " +
            "execution(* parse()) || " +
            "execution(* getBox(*)) || " +
            "execution(* getSize()) || " +
            "execution(* getOffset()) || " +
            "execution(* setOffset(*)) || " +
            "execution(* parseDetails()) || " +
            "execution(* _parseDetails(*)) || " +
            "execution(* parse(*,*,*,*)) || " +
            "execution(* getIsoFile()) || " +
            "execution(* getParent()) || " +
            "execution(* setParent(*)) || " +
            "execution(* getUserType()) || " +
            "execution(* setUserType(*))) && " +
            "!@annotation(org.mp4parser.support.DoNotParseDetail)) || @annotation(org.mp4parser.support.ParseDetail))")
    public void before(JoinPoint joinPoint) {
        if (joinPoint.getTarget() instanceof AbstractBox) {
            if (!((AbstractBox) joinPoint.getTarget()).isParsed()) {
                //System.err.println(String.format("parsed detail %s", joinPoint.getTarget().getClass().getSimpleName()));
                ((AbstractBox) joinPoint.getTarget()).parseDetails();
            }
        } else {
            throw new RuntimeException("Only methods in subclasses of " + AbstractBox.class.getName() + " can  be annotated with ParseDetail");
        }

    }


}