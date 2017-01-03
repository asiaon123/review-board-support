package com.guyazhou.tools.plugin.reviewboard.service;

import com.guyazhou.tools.plugin.reviewboard.vcs.GitVCSBuilder;
import com.guyazhou.tools.plugin.reviewboard.vcs.SVNVCSBuilder;
import com.guyazhou.tools.plugin.reviewboard.vcs.VCSBuilder;
import com.intellij.openapi.vcs.AbstractVcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * VCS factoty
 * Created by Yakov on 2016/12/28.
 */
public class VCSBuilderFactory {

    private static Map<String, Class<? extends VCSBuilder>> builder = new HashMap<>();

    static {
        builder.put("svn", SVNVCSBuilder.class);
        builder.put("Git", GitVCSBuilder.class);
    }

    public static VCSBuilder getVCSBuilder(AbstractVcs abstractVcs) {
        Class<? extends VCSBuilder> vcsBuilderClass = builder.get(abstractVcs.getName());
        if (null != vcsBuilderClass) {
            Constructor<? extends VCSBuilder> vcsBuilderConstructor = null;
            try {
                vcsBuilderConstructor = vcsBuilderClass.getConstructor(AbstractVcs.class);
                return vcsBuilderConstructor.newInstance(abstractVcs);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
