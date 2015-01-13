package com.example.rennt.arcticsunrise;

import com.example.rennt.arcticsunrise.data.DataModule;
import com.example.rennt.arcticsunrise.data.api.BaseApiPath;

import dagger.Module;
import dagger.Provides;

/**
 * Mock the EditionBasePath for debug builds
 */
@Module(
        overrides = true,
        library = true
)
public class MockApiModule {

      public enum Endpoint {
          PRODUCTION("Production", DataModule.PRODUCTION_API_URL),
          MOCK_MODE("Mock mode", "file://"),
          CUSTOM("Time lapse", null);

          private String name;
          private String url;

          Endpoint(String name, String url) {
              this.name = name;
              this.url = url;
          }

          @Override public String toString() {
              return name;
          }

          public static Endpoint from(String endpoint) {
              for (Endpoint value : values()) {
                  if (value.url != null && value.url.equals(endpoint)) {
                      return value;
                  }
              }
              return CUSTOM;
          }

          public static boolean isMockMode(String endpoint) {
              return from(endpoint) == MOCK_MODE;
          }
      }


//    @Provides @BaseApiPath String provideBaseApiPath(){
//        return "file://";
//    }


}
