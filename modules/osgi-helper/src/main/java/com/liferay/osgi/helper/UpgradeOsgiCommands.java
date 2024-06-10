package com.liferay.osgi.helper;

import com.liferay.petra.string.CharPool;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.util.Dictionary;
import java.util.Set;
import java.util.TreeSet;

@Component(
    property = {
            "osgi.command.function=checkImportPackageHeader",
            "osgi.command.scope=upgrade-utils"
    },
    service = Object.class
)
public class UpgradeOsgiCommands {

    public void checkImportPackageHeader(long bundleId) {
        Bundle bundle = _bundleContext.getBundle(bundleId);

        Dictionary<String, String> bundleHeaders = bundle.getHeaders();

        String importPackages = bundleHeaders.get("Import-Package");

        String[] importPackageEntries = importPackages.split(",");

        Set<String> notAvailablePackages = new TreeSet<>();

        for (String importPackageEntry : importPackageEntries) {
            String packageName = importPackageEntry.split(";")[0];

            if (_packageAdmin.getExportedPackage(packageName) == null) {
                notAvailablePackages.add(packageName);
            }
        }

        if (!notAvailablePackages.isEmpty()) {
            System.out.println("Recommended Import-Package header for " + bundle.getSymbolicName() + " module");
            System.out.println(buildImportPackageHeader(notAvailablePackages));
            System.out.println();
            System.out.println("****************************************");
            System.out.println("NOTE: If you face some NoClassDefException on runtime, add the dependency for that class and run the command again.");

        }
        else {
            System.out.println(
                    "All packages in the Import-Package header of " + bundle.getSymbolicName() + " module are available");
        }

    }

    private static String buildImportPackageHeader(Set<String> notAvailablePackages) {
        StringBuilder sb = new StringBuilder();

        sb.append("Import-Package: ");
        sb.append(CharPool.BACK_SLASH);
        sb.append(CharPool.NEW_LINE);

        for (String notAvailablePackage : notAvailablePackages) {
            sb.append(CharPool.TAB);
            sb.append(CharPool.EXCLAMATION);
            sb.append(notAvailablePackage);
            sb.append(CharPool.COMMA);
            sb.append(CharPool.BACK_SLASH);
            sb.append(CharPool.NEW_LINE);
        }

        sb.append(CharPool.TAB);
        sb.append(CharPool.BACK_SLASH);
        sb.append(CharPool.NEW_LINE);
        sb.append(CharPool.TAB);
        sb.append(CharPool.STAR);

        return sb.toString();
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        _bundleContext = bundleContext;
    }

    private BundleContext _bundleContext;

    @Reference
    private PackageAdmin _packageAdmin;

}
