/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample;

import static org.uberfire.java.nio.fs.jgit.util.JGitUtil.commit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.Git;
import org.junit.Test;
import org.uberfire.java.nio.fs.jgit.util.JGitUtil;
import org.uberfire.java.nio.fs.jgit.util.JGitUtil.JGitPathInfo;

public class ConcurrentJGitUtilTest 
{

    @Test
    public void testCommitAndResolveWithCleanRepo() throws IOException {

        final File parentFolder = createTempDirectory();
        final File gitFolder = new File(parentFolder, "mytest.git");

        System.out.println(gitFolder.getAbsolutePath());

        final Git git = JGitUtil.newRepository(gitFolder, true);

        commitAndResolve(git);
    }

    @Test
    public void testCommitAndResolveWithLargeRepo() throws IOException {

        final File parentFolder = createTempDirectory();
        File sourceDir = new File("testRepo/handson5user01Repo.git");

        copyDir(sourceDir, parentFolder);

        final File gitFolder = new File(parentFolder, "handson5user01Repo.git");

        System.out.println(gitFolder.getAbsolutePath());

        final Git git = JGitUtil.newRepository(gitFolder, true);

        commitAndResolve(git);
    }

    private static void copyDir(File sourceDir, File distParentDir) throws IOException {

        File[] sourceFiles = sourceDir.listFiles();
        File distDir = new File(distParentDir.getPath() + File.separator + sourceDir.getName());

        distDir.mkdir();

        if (sourceFiles != null) {

            for (File f : sourceFiles) {
                if (f.isFile()) {
                    copyFile(f, distDir);
                } else {
                    copyDir(f, distDir);
                }
            }
        }
    }

    public static void copyFile(File source, File targetDir) throws IOException {
        File target = new File(targetDir.getPath() + File.separator + source.getName());

        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

    }

    private void commitAndResolve(final Git git) throws IOException {
        final long start = System.currentTimeMillis();
        
        final int commiterCount = Integer.parseInt(System.getProperty("commiter.count"));
        System.out.println("commiter.count = " + commiterCount);

        commit(git, "master", "name", "name@example.com", "1st commit", null, new Date(), false, new HashMap<String, File>() {
            {
                put("path/to/file1.txt", tempFile("temp2222"));
            }
        });
        commit(git, "master", "name", "name@example.com", "2nd commit", null, new Date(), false, new HashMap<String, File>() {
            {
                put("path/to/file2.txt", tempFile("temp2222"));
            }
        });
        commit(git, "master", "name", "name@example.com", "3rd commit", null, new Date(), false, new HashMap<String, File>() {
            {
                put("path/to/file3.txt", tempFile("temp2222"));
            }
        });

        Runnable commiter = new Runnable() {
            @Override
            public void run() {
                System.out.println("commiter start");
                try {
                    for (int i = 0; i < commiterCount; i++) {
                        final int fileNum = i;
                        commit(git, "master", "name", "name@example.com", "commit with amend", null, new Date(), true, new HashMap<String, File>() {
                            {
                                put("path/to/additional-file" + fileNum + ".txt", tempFile("temp2222"));
                            }
                        });
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                System.out.println("commiter finished : elapsedTime = " + (System.currentTimeMillis() - start) + " ms");
            }
        };

        Runnable reader = new Runnable() {
            @Override
            public void run() {
                System.out.println("reader start");
                for (int i = 0; i < commiterCount * 10; i++) {
                    JGitPathInfo info = JGitUtil.resolvePath(git, "master", "path/to/file1.txt");
                    if (info == null) {
                        System.out.println("info == null, i = " + i);
                    } else if (info.getPath() == null) {
                        System.out.println("info.getPath() == null, i = " + i);
                    } else if (!info.getPath().equals("path/to/file1.txt")) {
                        System.out.println("info.getPath() == " + info.getPath() + ", i = " + i);
                    }
                }
                System.out.println("reader finished : elapsedTime = " + (System.currentTimeMillis() - start) + " ms");
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(commiter);
        executor.execute(reader);
        executor.shutdown();
        try {
            executor.awaitTermination(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    protected static File createTempDirectory()
            throws IOException {
        final File temp = File.createTempFile( "temp", Long.toString( System.nanoTime() ) );
        if ( !( temp.delete() ) ) {
            throw new IOException( "Could not delete temp file: " + temp.getAbsolutePath() );
        }

        if ( !( temp.mkdir() ) ) {
            throw new IOException( "Could not create temp directory: " + temp.getAbsolutePath() );
        }
        return temp;
    }

    public File tempFile( final String content ) throws IOException {
        final File file = File.createTempFile( "bar", "foo" );
        final OutputStream out = new FileOutputStream( file );

        if ( content != null && !content.isEmpty() ) {
            out.write( content.getBytes() );
            out.flush();
        }

        out.close();
        return file;
    }
}