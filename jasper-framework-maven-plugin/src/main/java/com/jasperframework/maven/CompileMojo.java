package com.jasperframework.maven;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compiles JRXML files to .jasper files during the build.
 *
 * <pre>{@code
 * <plugin>
 *     <groupId>com.jasperframework</groupId>
 *     <artifactId>jasper-framework-maven-plugin</artifactId>
 *     <version>${jasper-framework.version}</version>
 *     <executions>
 *         <execution>
 *             <goals>
 *                 <goal>compile</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 *     <configuration>
 *         <sourceDirectory>${project.basedir}/src/main/resources/reports</sourceDirectory>
 *         <outputDirectory>${project.build.outputDirectory}/reports</outputDirectory>
 *     </configuration>
 * </plugin>
 * }</pre>
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CompileMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/resources/reports")
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}/reports")
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!sourceDirectory.exists()) {
            getLog().info("Source directory does not exist, skipping: " + sourceDirectory);
            return;
        }

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new MojoExecutionException("Cannot create output directory: " + outputDirectory);
        }

        File[] jrxmlFiles = sourceDirectory.listFiles((dir, name) -> name.endsWith(".jrxml"));
        if (jrxmlFiles == null || jrxmlFiles.length == 0) {
            getLog().info("No JRXML files found in: " + sourceDirectory);
            return;
        }

        getLog().info("Compiling " + jrxmlFiles.length + " JRXML files from " + sourceDirectory);
        int compiled = 0;

        for (File jrxml : jrxmlFiles) {
            String baseName = jrxml.getName().replace(".jrxml", ".jasper");
            File output = new File(outputDirectory, baseName);

            try {
                getLog().debug("Compiling: " + jrxml.getName());
                JasperCompileManager.compileReportToFile(
                        jrxml.getAbsolutePath(), output.getAbsolutePath());
                compiled++;
                getLog().info("Compiled: " + jrxml.getName() + " -> " + baseName);
            } catch (JRException e) {
                throw new MojoFailureException("Failed to compile: " + jrxml.getName(), e);
            }
        }

        getLog().info("Successfully compiled " + compiled + " JRXML files");
    }
}
