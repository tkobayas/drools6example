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

import static org.junit.Assert.*;
import static org.uberfire.java.nio.fs.jgit.util.JGitUtil.commit;
import static org.uberfire.java.nio.fs.jgit.util.JGitUtil.PathType.NOT_FOUND;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;
import org.uberfire.commons.data.Pair;
import org.uberfire.java.nio.fs.jgit.util.JGitUtil;
import org.uberfire.java.nio.fs.jgit.util.JGitUtil.JGitPathInfo;
import org.uberfire.java.nio.fs.jgit.util.JGitUtil.PathType;

public class MultiThreadJGitUtilTest {
    static int committerCount = 0;

    static {
        //      System.setProperty("org.uberfire.nio.git.retry.onfail.sleep", "200");
        //      System.setProperty("org.uberfire.nio.git.retry.onfail.times", "30");

        committerCount = Integer.parseInt(System.getProperty("committer.count", "200"));

        System.out.println("committer.count = " + committerCount);

        System.out.println("org.uberfire.nio.git.retry.onfail.sleep : " + System.getProperty("org.uberfire.nio.git.retry.onfail.sleep"));
        System.out.println("org.uberfire.nio.git.retry.onfail.times : " + System.getProperty("org.uberfire.nio.git.retry.onfail.times"));
    }

    @Test
    public void testCommitAndResolvePath() throws IOException {

        System.out.println("=== testCommitAndResolvePath");

        final File parentFolder = createTempDirectory();
        final File gitFolder = new File(parentFolder, "mytest.git");

        System.out.println(gitFolder.getAbsolutePath());

        final Git git = JGitUtil.newRepository(gitFolder, true);

        final Set<Integer> failureSet = new HashSet<Integer>();

        Runnable reader = new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                System.out.println("reader start");
                for (int i = 0; i < committerCount * 10; i++) {
                    JGitPathInfo info = JGitUtil.resolvePath(git, "master", "path/to/file1.txt");
                    if (info == null) {
                        System.out.println("info == null, i = " + i);
                        failureSet.add(i);
                    } else if (info.getPath() == null) {
                        System.out.println("info.getPath() == null, i = " + i);
                        failureSet.add(i);
                    } else if (!info.getPath().equals("path/to/file1.txt")) {
                        System.out.println("info.getPath() == " + info.getPath() + ", i = " + i);
                        failureSet.add(i);
                    }
                }
                System.out.println("reader finished : elapsedTime = " + (System.currentTimeMillis() - start) + " ms");
            }
        };

        commitAndDoSomething(git, reader);

        assertEquals(0, failureSet.size());
    }

    @Test
    public void testCommitAndResolveInputStream() throws IOException {

        System.out.println("=== testCommitAndResolveInputStream");

        final File parentFolder = createTempDirectory();
        final File gitFolder = new File(parentFolder, "mytest.git");

        System.out.println(gitFolder.getAbsolutePath());

        final Git git = JGitUtil.newRepository(gitFolder, true);

        final Set<Integer> failureSet = new HashSet<Integer>();

        Runnable reader = new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                System.out.println("reader start");
                for (int i = 0; i < committerCount * 10; i++) {
                    InputStream is = JGitUtil.resolveInputStream(git, "master", "path/to/file1.txt");
                    if (is == null) {
                        System.out.println("is == null, i = " + i);
                        failureSet.add(i);
                    }
                }
                System.out.println("reader finished : elapsedTime = " + (System.currentTimeMillis() - start) + " ms");
            }
        };

        commitAndDoSomething(git, reader);

        assertEquals(0, failureSet.size());
    }

    @Test
    public void testCommitAndListPathContent() throws IOException {

        System.out.println("=== testCommitAndListPathContent");

        final File parentFolder = createTempDirectory();
        final File gitFolder = new File(parentFolder, "mytest.git");

        System.out.println(gitFolder.getAbsolutePath());

        final Git git = JGitUtil.newRepository(gitFolder, true);

        final Set<Integer> failureSet = new HashSet<Integer>();

        Runnable reader = new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                System.out.println("reader start");
                for (int i = 0; i < committerCount * 10; i++) {
                    List<JGitPathInfo> infoList = JGitUtil.listPathContent(git, "master", "path/to/");
                    if (infoList == null) {
                        System.out.println("infoList == null, i = " + i);
                        failureSet.add(i);
                    } else if (infoList.size() == 0) {
                        System.out.println("infoList.size() == 0, i = " + i);
                        failureSet.add(i);
                    }
                }
                System.out.println("reader finished : elapsedTime = " + (System.currentTimeMillis() - start) + " ms");
            }
        };

        commitAndDoSomething(git, reader);

        assertEquals(0, failureSet.size());
    }

    @Test
    public void testCommitAndCheckPath() throws IOException {

        System.out.println("=== testCommitAndCheckPath");

        final File parentFolder = createTempDirectory();
        final File gitFolder = new File(parentFolder, "mytest.git");

        System.out.println(gitFolder.getAbsolutePath());

        final Git git = JGitUtil.newRepository(gitFolder, true);

        final Set<Integer> failureSet = new HashSet<Integer>();

        Runnable reader = new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                System.out.println("reader start");
                for (int i = 0; i < committerCount * 10; i++) {
                    Pair<PathType, ObjectId> pathResult = JGitUtil.checkPath(git, "master", "path/to/file1.txt");
                    if (pathResult == null) {
                        System.out.println("pathResult == null, i = " + i);
                        failureSet.add(i);
                    } else if (pathResult.getK1().equals(NOT_FOUND)) {
                        System.out.println("pathResult.getK1().equals( NOT_FOUND ), i = " + i);
                        failureSet.add(i);
                    }
                }
                System.out.println("reader finished : elapsedTime = " + (System.currentTimeMillis() - start) + " ms");
            }
        };

        commitAndDoSomething(git, reader);

        assertEquals(0, failureSet.size());
    }

    @Test
    public void testCommitAndGetLastCommit() throws IOException {

        System.out.println("=== testCommitAndGetLastCommit");

        final File parentFolder = createTempDirectory();
        final File gitFolder = new File(parentFolder, "mytest.git");

        System.out.println(gitFolder.getAbsolutePath());

        final Git git = JGitUtil.newRepository(gitFolder, true);

        final Set<Integer> failureSet = new HashSet<Integer>();

        Runnable reader = new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                System.out.println("reader start");
                for (int i = 0; i < committerCount * 10; i++) {
                    RevCommit lastCommit = JGitUtil.getLastCommit(git, "master");
                    if (lastCommit == null) {
                        System.out.println("lastCommit == null, i = " + i);
                        failureSet.add(i);
                    }
                }
                System.out.println("reader finished : elapsedTime = " + (System.currentTimeMillis() - start) + " ms");
            }
        };

        commitAndDoSomething(git, reader);

        assertEquals(0, failureSet.size());
    }

    private void commitAndDoSomething(final Git git, Runnable reader) throws IOException {

        // prepare some commits
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

        // committer thread
        Runnable committer = new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                System.out.println("committer start");
                try {
                    for (int i = 0; i < committerCount; i++) {
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
                System.out.println("committer finished : elapsedTime = " + (System.currentTimeMillis() - start) + " ms");
            }
        };

        // reader thread is defined in each test method

        // Execute!
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(committer);
        executor.execute(reader);
        executor.shutdown();
        try {
            executor.awaitTermination(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static File createTempDirectory() throws IOException {
        final File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return temp;
    }

    public File tempFile(final String content) throws IOException {
        final File file = File.createTempFile("bar", "foo");
        final OutputStream out = new FileOutputStream(file);

        if (content != null && !content.isEmpty()) {
            out.write(content.getBytes());
            out.flush();
        }

        out.close();
        return file;
    }
}