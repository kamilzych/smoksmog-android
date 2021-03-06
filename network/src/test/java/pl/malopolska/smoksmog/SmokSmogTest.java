package pl.malopolska.smoksmog;

import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class SmokSmogTest {

    @Test
    public void testCreateServerUrlWithLocale() throws Exception {

        // given
        String endpointUrl = "http://api.smoksmog.jkostrz.name/";
        Locale locale = Locale.ENGLISH;

        // when
        String result = SmokSmog.createEndpoint( endpointUrl, locale );

        // then
        assertThat( result ).isEqualTo( "http://api.smoksmog.jkostrz.name/en" );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testCreateInvalidServerUrlWithLocale() throws Exception {

        // given
        String endpointUrl = "htkostrz.name/";
        Locale locale = Locale.FRANCE;

        // when
        SmokSmog.createEndpoint( endpointUrl, locale );

        // then
        // exception
    }
}