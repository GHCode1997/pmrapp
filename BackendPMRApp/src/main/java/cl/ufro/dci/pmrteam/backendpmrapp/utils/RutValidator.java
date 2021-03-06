/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.ufro.dci.pmrteam.backendpmrapp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ghuerta
 */
public class RutValidator {

    /**
     * Valida rut de la forma XXXXXXXX-X or XX.XXX.XXX-X
     */
    public static Boolean validaRut(String rut) {
        String[] run = rut.split("");
        String newRun = "";
        for (int i = 0; i < run.length; i++) {
            if (!".".equals(run[i])) {
                newRun += run[i];
            }
        }
        Pattern pattern = Pattern.compile("^[0-9]+-[0-9kK]{1}$");
        Matcher matcher = pattern.matcher(newRun);
        if (matcher.matches() == false) {
            return false;
        }
        String[] stringRut = newRun.split("-");
        return stringRut[1].toLowerCase().equals(RutValidator.dv(stringRut[0]));
    }

    /**
     * Valida el dígito verificador
     */
    public static String dv(String rut) {
        Integer M = 0, S = 1, T = Integer.parseInt(rut);
        for (; T != 0; T = (int) Math.floor(T /= 10)) {
            S = (S + T % 10 * (9 - M++ % 6)) % 11;
        }
        return (S > 0) ? String.valueOf(S - 1) : "k";
    }
}
