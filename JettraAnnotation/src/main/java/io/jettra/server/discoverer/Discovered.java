/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.jettra.server.discoverer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 *
 * @author avbravo
 */


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Discovered {
boolean automatic() default true;
}    
