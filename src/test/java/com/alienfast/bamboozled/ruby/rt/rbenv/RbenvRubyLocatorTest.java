package com.alienfast.bamboozled.ruby.rt.rbenv;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.alienfast.bamboozled.ruby.rt.RubyRuntime;
import com.alienfast.bamboozled.ruby.rt.rbenv.RbenvRubyLocator;
import com.alienfast.bamboozled.ruby.util.EnvUtils;
import com.alienfast.bamboozled.ruby.util.FileSystemHelper;
import com.alienfast.bamboozled.ruby.util.PathNotFoundException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Test the rbenv Ruby Locator
 */
@RunWith( MockitoJUnitRunner.class )
public class RbenvRubyLocatorTest {

    final String badGemHomePath = "/usr/lib/ruby/home";

    RbenvRubyLocator rbenvRubyLocator;

    @Mock
    private FileSystemHelper fileSystemHelper;

    final static RubyRuntime ruby192p290default = new RubyRuntime(
            "1.9.2-p290",
            "default",
            "/Users/kross/.rbenv/versions/1.9.2-p290/bin/ruby",
            null );
    final static String rubyExecutablePath = ruby192p290default.getRubyExecutablePath();

    @Before
    public void setUp() throws Exception {

        this.rbenvRubyLocator = new RbenvRubyLocator( this.fileSystemHelper, "/Users/kross/.rbenv" );
        when( this.fileSystemHelper.getUserHome() ).thenReturn( "/Users/kross" );

    }

    @Test
    public void testBuildEnv() throws Exception {

        // seeding the env with stuff that screw up
        // us running ruby from rbenv installation.
        Map<String, String> existingEnv = Maps.newHashMap();
        existingEnv.put( EnvUtils.GEM_HOME, this.badGemHomePath );
        existingEnv.put( EnvUtils.PATH, "/usr/bin:/bin:/usr/sbin:/sbin" );

        Map<String, String> updatedEnv = this.rbenvRubyLocator.buildEnv( "1.9.2-p290@default", rubyExecutablePath, existingEnv );

        assertThat( updatedEnv.containsKey( EnvUtils.GEM_HOME ), equalTo( false ) );

        assertThat( updatedEnv.get( EnvUtils.GEM_HOME ), nullValue() );
        assertThat( updatedEnv.get( EnvUtils.GEM_PATH ), nullValue() );

        assertThat( updatedEnv.get( EnvUtils.PATH ), equalTo( "/Users/kross/.rbenv/versions/1.9.2-p290/bin:/usr/bin:/bin:/usr/sbin:/sbin" ) );

    }

    @Test
    public void testGetRubyRuntimeByVersionAndGemset() throws Exception {

        assertThat( this.rbenvRubyLocator.getRubyRuntime( "1.9.2-p290", "default" ), equalTo( ruby192p290default ) );
    }

    @Test( expected = PathNotFoundException.class )
    public void testGetRubyRuntimeByVersionAndGemsetWithMissingRuby() throws Exception {

        doThrow( new PathNotFoundException( "Some exception" ) ).when( this.fileSystemHelper ).assertPathExists(
                ruby192p290default.getRubyExecutablePath(),
                "Unable to location ruby executable for " + ruby192p290default.getName() );

        this.rbenvRubyLocator.getRubyRuntime( "1.9.2-p290", "default" );

    }

    @Test
    public void testGetRubyRuntimeByRuntimeName() throws Exception {

        assertThat( this.rbenvRubyLocator.getRubyRuntime( "1.9.2-p290@default" ), equalTo( ruby192p290default ) );

    }

    @Test
    public void testListRubyRuntimes() throws Exception {

        when( this.fileSystemHelper.listPathDirNames( eq( "/Users/kross/.rbenv/versions" ) ) ).thenReturn(
                Lists.newArrayList( "1.9.2-p180", "1.9.2-p290", "ree-1.8.7-2011.12" ) );

        assertThat( this.rbenvRubyLocator.listRubyRuntimes(), hasItems( ruby192p290default ) );

    }

    @Test
    public void testHasRuby() throws Exception {

        when( this.fileSystemHelper.pathExists( ruby192p290default.getRubyExecutablePath() ) ).thenReturn( true );

        assertThat( this.rbenvRubyLocator.hasRuby( "1.9.2-p290@default" ), equalTo( true ) );

        when( this.fileSystemHelper.pathExists( ruby192p290default.getRubyExecutablePath() ) ).thenReturn( false );

        assertThat( this.rbenvRubyLocator.hasRuby( "1.9.2-p290@default" ), equalTo( false ) );

    }

    @Test
    public void testIsReadOnly() throws Exception {

    }
}
