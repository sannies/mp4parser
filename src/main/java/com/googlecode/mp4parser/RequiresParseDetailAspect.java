package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.AbstractBox;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class RequiresParseDetailAspect {


    @Before("this(com.coremedia.iso.boxes.AbstractBox) && ((execution(public * * (..)) && !( " +
            "execution(* parseDetails()) || " +
            "execution(* getNumOfBytesToFirstChild()) || " +
            "execution(* getType()) || " +
            "execution(* isParsed()) || " +
            "execution(* getHeader(*)) || " +
            "execution(* parse()) || " +
            "execution(* getBox(*)) || " +
            "execution(* getSize()) || " +
            "execution(* parseDetails()) || " +
            "execution(* _parseDetails(*)) || " +
            "execution(* parse(*,*,*,*)) || " +
            "execution(* getIsoFile()) || " +
            "execution(* getParent()) || " +
            "execution(* setParent(*)) || " +
            "execution(* getUserType()) || " +
            "execution(* setUserType(*))) && " +
            "!@annotation(DoNotParseDetail)) || @annotation(ParseDetail))")
    public void before(JoinPoint joinPoint) {
        if (joinPoint.getTarget() instanceof AbstractBox) {
            if (!((AbstractBox) joinPoint.getTarget()).isParsed()) {
                //System.err.println(String.format("parsed detail %s", joinPoint.getTarget().getClass().getSimpleName()));
                ((AbstractBox) joinPoint.getTarget()).parseDetails();
            }
        } else {
            throw new RuntimeException("Only methods in subclasses of " + AbstractBox.class.getName() + " can  be annotated with DoNotParseDetail");
        }

    }


}