package uniresolver.local.extensions.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.DIDURL;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.local.extensions.Extension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.Extension.AbstractExtension;
import uniresolver.result.ResolveResult;

public class RedirectExtension extends AbstractExtension implements Extension {

	private static Logger log = LoggerFactory.getLogger(RedirectExtension.class);

	@Override
	public ExtensionStatus afterResolve(String identifier, DIDURL didUrl, Map<String, String> options, ResolveResult resolveResult, LocalUniResolver localUniResolver) throws ResolutionException {

		if (! resolveResult.getMethodMetadata().containsKey("redirect")) return ExtensionStatus.DEFAULT;

		while (resolveResult.getMethodMetadata().containsKey("redirect")) {

			String resolveIdentifier = (String) resolveResult.getMethodMetadata().get("redirect");
			if (log.isDebugEnabled()) log.debug("Resolving identifier: " + resolveIdentifier);

			ResolveResult driverResolveResult = localUniResolver.resolveWithDrivers(resolveIdentifier);

			if (driverResolveResult != null) {

				ResolveResult previousResolveResult = ResolveResult.build(resolveResult.getDidDocument(), resolveResult.getResolverMetadata(), resolveResult.getMethodMetadata());
				resolveResult.getResolverMetadata().clear();
				resolveResult.getResolverMetadata().put("previous", previousResolveResult);

				resolveResult.setDidDocument(driverResolveResult.getDidDocument());
				resolveResult.setMethodMetadata(driverResolveResult.getMethodMetadata());

				resolveResult.getResolverMetadata().putAll(driverResolveResult.getResolverMetadata());
			} else {

				break;
			}
		}

		return ExtensionStatus.DEFAULT;
	}
}
