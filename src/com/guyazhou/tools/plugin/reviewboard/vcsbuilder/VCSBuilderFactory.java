package com.guyazhou.tools.plugin.reviewboard.vcsbuilder;

import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.git.GitVcsBuilder;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.svn.SvnVcsBuilder;
import com.intellij.openapi.vcs.AbstractVcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Support svn, Git
 * Created by Yakov on 2016/12/28.
 */
public class VCSBuilderFactory {

    private static Map<String, Class<? extends VcsBuilder>> builder = new HashMap<>();

    static {
        builder.put("svn", SvnVcsBuilder.class);
        builder.put("Git", GitVcsBuilder.class);
    }

    /**
     * Get a VcsBuilder instance according to the abstractVcs
     * @param abstractVcs abstractVcs
     * @return A VcsBuilder instance
     * @throws Exception exception
     */
    public static VcsBuilder getVCSBuilder(AbstractVcs abstractVcs) throws Exception {

        Class<? extends VcsBuilder> vcsBuilderClass = builder.get(abstractVcs.getName());
        if (null != vcsBuilderClass) {
            Constructor<? extends VcsBuilder> vcsBuilderConstructor;
            try {
                vcsBuilderConstructor = vcsBuilderClass.getConstructor(AbstractVcs.class);
                return vcsBuilderConstructor.newInstance(abstractVcs);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new Exception("Get VcsBuilder instance error, " + e.getMessage());
            }
        }
        return null;
    }

}
