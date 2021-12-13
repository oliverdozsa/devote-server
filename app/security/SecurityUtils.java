package security;

import play.mvc.Http;
import security.filter.Attrs;

public class SecurityUtils {
    // Assumes that JwtFilter added the verified JWT as attribute to the request.
    public static VerifiedJwt getFromRequest(Http.Request httpRequest) {
        // TODO: remove this
        return new VerifiedJwt() {
            @Override
            public String getUserId() {
                return "someUserId";
            }
        };

        // return httpRequest.attrs().get(Attrs.VERIFIED_JWT);
    }
}
