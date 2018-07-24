package com.guyazhou.tools.plugin.reviewboard.vcsbuilder;

import com.guyazhou.tools.plugin.reviewboard.exceptions.NoVcsProviderFoundException;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.git.GitVcsProvider;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.svn.SvnVcsProvider;
import com.intellij.openapi.vcs.AbstractVcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Vcs provider factoty, support svn, Git now.
 *
 * @author YaZhou.Gu 2016/12/28
 */
public class VcsProviderFactory {

    private static Map<String, Class<? extends VcsProvider>> vcsProviders = new HashMap<>();

    static {
        vcsProviders.put("svn", SvnVcsProvider.class);
        vcsProviders.put("Git", GitVcsProvider.class);
    }

    /**
     * Get the vcs provider for vcs
     *
     * @param abstractVcs abstractVcs
     * @return a VcsProvider instance
     */
    public static VcsProvider getVcsProvider(AbstractVcs abstractVcs) {
        Class<? extends VcsProvider> vcsProviderClazz = vcsProviders.get(abstractVcs.getName());
        if (vcsProviderClazz == null) {
            throw new NullPointerException(String.format("No vcs provider found for [ %s ]", abstractVcs.getName()));
        }
        Constructor<? extends VcsProvider> vcsProviderClazzConstructor;
        try {
            vcsProviderClazzConstructor = vcsProviderClazz.getConstructor(AbstractVcs.class);
            return vcsProviderClazzConstructor.newInstance(abstractVcs);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new NoVcsProviderFoundException(String.format("Can not initialize vcs provider for [ %s ]", vcsProviderClazz.getName()), e);
        }
    }

}
