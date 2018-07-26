package com.guyazhou.plugin.reviewboard.vcsprovider;

import com.guyazhou.plugin.reviewboard.vcsprovider.git.GitVcsProvider;
import com.guyazhou.plugin.reviewboard.vcsprovider.svn.SvnVcsProvider;
import com.guyazhou.plugin.reviewboard.exceptions.NoVcsProviderFoundException;
import com.intellij.openapi.vcs.AbstractVcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Vcs provider factory, support svn, Git now.
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
     * @return a vcs provider instance
     */
    public static VcsProvider getVcsProvider(AbstractVcs abstractVcs) {
        Class<? extends VcsProvider> vcsProviderClazz = vcsProviders.get(abstractVcs.getName());
        if (vcsProviderClazz == null) {
            throw new NullPointerException(String.format("No vcs provider found for [ %s ]", abstractVcs.getName()));
        }
        try {
            Constructor<? extends VcsProvider> vcsProviderClazzConstructor = vcsProviderClazz.getConstructor(AbstractVcs.class);
            return vcsProviderClazzConstructor.newInstance(abstractVcs);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new NoVcsProviderFoundException(String.format("Can not initialize vcs provider for [ %s ]", vcsProviderClazz.getName()), e);
        }
    }

}
