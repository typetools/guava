This is a version of Guava that is annotated with type annotations for the Checker Framework.

The annotations are only in the main Guava project, not in the "Android" variant that supports JDK 7 and Android.


To build this project
---------------------

To create file `guava/target/guava-HEAD-jre-SNAPSHOT.jar`:
(This takes 10+ minutes, because it performs pluggable type-checking.)

```
(cd guava && ../mvnw -B package -Dmaven.test.skip=true -Danimal.sniffer.skip=true)
```

To use a locally-built Checker Framework:
```
(cd guava && ../mvnw -B package -Dmaven.test.skip=true -Danimal.sniffer.skip=true -P checkerframework-local)
```
or change `guava/pom.xml`.


To run the Guava test suite
---------------------------

This not strictly necessary as the CI process will run them. But if you wish to
do so, it is a two-step process. This can take a long time: 20-30 minutes for
the compile step and 30-35 minutes for the test step.  In order to shorten it a
bit, the command lines below only run a single checker (the one for format
strings).  First, you must run maven with the install target to get a copy of
all the generated jar files in your local maven repository:

./mvnw -V -B -U clean install -Dcheckerframework.checkers=org.checkerframework.checker.formatter.FormatterChecker  \
    -Dcheck.value.phase=skip -Dmaven.test.skip -Dmaven.javadoc.skip

Then you run the test suite:

MAVEN_OPTS=-Xmx6g ./mvnw -V -B -U verify -Dcheckerframework.checkers=org.checkerframework.checker.formatter.FormatterChecker \
                      -Dcheck.value.phase=skip -Dmaven.javadoc.skip

Running with the single FormatterChecker gets the total time down to about 20-25 minutes,
almost all of which testing. (Over 850,000 tests are run.)


Typechecking
------------

The Maven properties in guava/pom.xml can be used to change the behavior:

- `checkerframework.checkers` defines which checkers are run during compilation
- `checkerframework.suppress` defines warning keys suppressed during compilation
- `checkerframework.index.packages` defines packages checked by the Index Checker

- `checkerframework.extraargs` defines additional argument passed to the checkers during compilation, for example `-Ashowchecks`.
- `checkerframework.extraargs2` defines additional argument passed to the checkers during compilation, for example `-Aannotations`.
- `index.only.arg` defines additional argument passed to the Index Checker, for example `-Ashowchecks`.

Only the packages `com.google.common.primitives`, `com.google.common.base`,
`com.google.common.escape`, `com.google.common.math`,
`com.google.common.io`, and `com.google.common.hash` are annotated by Index
Checker annotations.  In order to get implicit annotations in class files,
the Index Checker runs on all files during compilation, but warnings are
suppressed. The Index Checker is run in another phase to typecheck just
these annotated packages. If there are errors, then the build fails.


Typechecking against the master branch of the Checker Framework
---------------------------------------------------------------

The released version of the Checker Framework may be incompatible with the
development version in the GitHub repository
https://github.com/typetools/checker-framework/tree/master .  For example,
the GitHub version may have renamed or added annotations or error messages.

When a pull request depends on forthcoming Checker Framework features, make
pull requests against a branch named `cf-master`.  Its Travis job uses the
Checker Framework from GitHub, rather than a Checker Framework release.

To create the `cf-master` branch if it does not exist:
It should differ only in file `.travis.yml`, which should pass
```
  "-P checkerframework-local"
```
(with the quotes) to `./.travis-build.sh`.

Whenever a Checker Framework release is made:
 * undo the change in `.travis.yml`,
 * update the Checker Framework version number,
 * pull-request the `cf-master` branch into `master`, and
 * delete the `cf-master` branch.


To release to Maven Central
---------------------------

(Note about re-releasing:  You can re-release a given version of Guava because
you have added annotations or because the Checker Framework has changed.
However, if you have pulled, from upstream, *commits subsequent to the release*,
then you will need to do something special.  Those instructions are not yet
written.)

1. Use the latest Checker Framework release
(https://github.com/typetools/checker-framework/releases):
 * Change `pom.xml` (in 1 place) and `guava/pom.xml` (in 1 place).
 * Re-build to ensure that typechecking still works:
   (cd guava && ../mvnw -B package -Dmaven.test.skip=true -Danimal.sniffer.skip=true)
 * Commit and push.
 * If a `cf-master` branch exists in this repository, follow the
   instructions above to merge it into master.

2. Pull in the latest Guava version (https://github.com/google/guava/releases).
This will makes a merge, which must appear in the typetools/guava repository.
(Do not squash-and-merge if you are working on a different branch!)
If your clone of guava is in directory `$t/libraries` and is named
`guava-fork-typetools`, then these commands will create a copy of the clone (not
a new branch) and pull in the upstream changes:

```
cd $t/libraries
GUAVA_FORK_TYPETOOLS=guava-fork-typetools
VER=33.1.0
rm -rf ${GUAVA_FORK_TYPETOOLS}-version-${VER}
cp -pr ${GUAVA_FORK_TYPETOOLS} ${GUAVA_FORK_TYPETOOLS}-version-${VER}
cd ${GUAVA_FORK_TYPETOOLS}-version-${VER}
git fetch --tags https://github.com/google/guava
git pull https://github.com/google/guava v${VER}
```

3. Ensure the `<dependencies>` sections are the same as in the upstream version:
```
diff -u \
  <(sed -n '/^  <dependencies>/,/^  <\/dependencies>/p' ../guava-fork-google/guava/pom.xml) \
  <(sed -n '/^  <dependencies>/,/^  <\/dependencies>/p' guava/cfMavenCentral.xml)
```

[comment]: # (The above sed commend is from https://stackoverflow.com/questions/38972736)

3. Resolve conflicts.  If you use Emacs, create a TAGS table to help:
```
etags $(git ls-files)
```

4. Ensure that the project still builds:
```
(cd guava && ../mvnw -B package -Dmaven.test.skip=true -Danimal.sniffer.skip=true)
```

5. Follow the instructions in section "to compare to upstream".

6. Update the Guava version number
 * in this file, and
 * in file guava/cfMavenCentral.xml .

If you are doing a re-release (that is, the version number is not the same as
the upstream version), then edit those files and also pom.xml and guava/pom.xml.

7. Commit and push changes.

8. Run the following commands (after adjusting the value of `PACKAGE`).

JAVA_HOME must be a JDK 8 JDK.
This step must be done on a machine, such as a CSE machine, that has access to the necessary passwords.

**NOTE**: maven-gpg-plugin version 3.2.1 is buggy
(https://issues.apache.org/jira/browse/MGPG-113) and will fail the upload with
"Failed to execute goal
org.apache.maven.plugins:maven-gpg-plugin:3.2.1:sign-and-deploy-file ... reason
phrase: Unauthorized (401)".  If version 3.2.1 is being used, change
"gpg:sign-and-deploy-file" to
"org.apache.maven.plugins:maven-gpg-plugin:3.1.0:sign-and-deploy-file".

TODO: Try replacing "-Dgpg.passphrase=..." by
```
export MAVEN_GPG_PASSPHRASE="$(cat $HOSTING_INFO_DIR/release-private.password)"
```

```
PACKAGE=guava-33.1.0.1-jre

cd guava

# Compile, and create Javadoc jar file (`../mvnw clean` removes MANIFEST.MF).
# This takes about 20 minutes.
[ ! -z "$PACKAGE" ] && \
../mvnw clean && \
../mvnw package -Dmaven.test.skip=true -Danimal.sniffer.skip=true && \
../mvnw source:jar && \
../mvnw javadoc:javadoc && (cd target/site/apidocs && jar -cf ${PACKAGE}-javadoc.jar com)

if [ -d /projects/swlab1/checker-framework/hosting-info ] ; then
  HOSTING_INFO_DIR=/projects/swlab1/checker-framework/hosting-info
elif [ -d $HOME/private/cf-hosting-info ] ; then
  HOSTING_INFO_DIR=$HOME/private/cf-hosting-info
else
  echo "Cannot set HOSTING_INFO_DIR."
  exit
fi


## These commands only need be run once ever.
# gpg --import ${HOSTING_INFO_DIR}/private-key.pgp
# gpg --import ${HOSTING_INFO_DIR}/public-key.pgp


[ ! -z "$PACKAGE" ] && \
../mvnw gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=cfMavenCentral.xml -Dgpg.publicKeyring=$HOSTING_INFO_DIR/pubring.gpg -Dgpg.secretKeyring=$HOSTING_INFO_DIR/secring.gpg -Dgpg.keyname=ADF4D638 -Dgpg.passphrase="`cat $HOSTING_INFO_DIR/release-private.password`" -Dfile=target/${PACKAGE}.jar \
&& \
../mvnw gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=cfMavenCentral.xml -Dgpg.publicKeyring=$HOSTING_INFO_DIR/pubring.gpg -Dgpg.secretKeyring=$HOSTING_INFO_DIR/secring.gpg -Dgpg.keyname=ADF4D638 -Dgpg.passphrase="`cat $HOSTING_INFO_DIR/release-private.password`" -Dfile=target/${PACKAGE}-sources.jar -Dclassifier=sources \
&& \
../mvnw gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=cfMavenCentral.xml -Dgpg.publicKeyring=$HOSTING_INFO_DIR/pubring.gpg -Dgpg.secretKeyring=$HOSTING_INFO_DIR/secring.gpg -Dgpg.keyname=ADF4D638 -Dgpg.passphrase="`cat $HOSTING_INFO_DIR/release-private.password`" -Dfile=target/site/apidocs/${PACKAGE}-javadoc.jar -Dclassifier=javadoc
```

`-Dgpg.keyname` is the last 8 characters of the fingerprint printed by `gpg --list-secret-keys`.

9. Complete the release at https://oss.sonatype.org/#stagingRepositories


To compare to upstream
----------------------

If you wish to see a simplified diff between this fork of Guava and upstream (to
make sure that you did not make any mistakes when resolving merge conflicts):

 * Clone both upstream and this fork.
 * Make a copy of each clone, because you will destructively edit each one.
 * Check each clone out to an appropriate commit.
 * Run the following commands in each temporary clone
   (preplace is in https://github.com/plume-lib/plume-scripts):
```
   ./mvnw -B clean
   rm -rf .git

   preplace '^import org.checker.*\n' ''

   # Annotations that take up an entire line
   preplace '^\@AnnotatedFor.*\n' ''
   preplace '^\@Covariant\([0-9]+\)\n' ''
   preplace '^ *\@(AssertMethod|CFComment|EnsuresLTLengthOf(If)?|HasSubsequence)\([^()]+\)\n' ''
   preplace '^ *\@FormatMethod\n' ''
   # preplace '^ *\@SuppressWarnings.*\n' ''
   preplace '^ *\@SuppressWarnings\(\{?"(cast\.unsafe|expression\.unparsable|index|lowerbound|nullness|override\.return|samelen|signature|signedness|substringindex|upperbound|value).*\n' ''
   preplace '^ *\@(Pure|Deterministic|SideEffectFree)\n' ''

   # Annotations that take no argument
   preplace '\@(GTENegativeOne|NonNegative|NonNull|Nullable|Poly[A-Za-z0-9_]+|PolySigned|Positive|Signed|SignednessGlb|SignedPositive|Unsigned|UnknownSignedness) ' ''

   # Annotations that take an argument
   preplace '\@(Format|IndexFor|IndexOr(Low|High)|IntRange|IntVal|KeyFor|LessThan|(|LT|LTEq)LengthOf|SubstringIndexFor)\([^()]+\) ' ''

   # Array-related spacing
   preplace '\@(Array|Min|Same)Len\([^()]+\) ?' ''
   preplace ' \[\]' '[]'
   preplace '[^*] \.\.\.' '...'

   preplace '([;{]) *// *\([0-9]+\)$' '\1'
   preplace '([;{]) *// *#[0-9]+$' '\1'

   # Extra syntax no longer needed
   preplace ' extends Object>' '>'
   preplace ' extends Object,' ','
   preplace '\([A-Za-z0-9_.]+(<[A-Z](, ?[A-Z])*>(.[A-Za-z0-9_.]+)?)? this\)' '()'

```
 * Diff the two temporary clones.


To update to a non-released version of the upstream library
-----------------------------------------------------------

Usually, you will want to update to a released version of the upstream library,
in which case see the instructions at "To release to Maven Central".  This
section is for updating to a commit in Guava that is different than the tag for
a release.  Note that doing so makes it harder to make a new release of the
typetools version of Guava at Maven Central.

First, update to use the latest Checker Framework by editing file
`pom.xml` (for `checker-qual`) and `guava/pom.xml` (for `checker`).
Make a pull request to ensure that type-checking succeeds.

Check for a release at
  https://github.com/google/guava/releases
.  If there has been one since the last time this repository was pulled,
then follow the instructions at "To release to Maven Central".

**After** checking and possibly releasing, run

```
git pull https://github.com/google/guava
```

and then re-build to ensure that typechecking still works.
Note: Doing this `git pull` command
makes it difficult to re-release a given version of Guava,
compared to pulling in the tag corresponding to a release.

Follow the instructions in section "to compare to upstream".
