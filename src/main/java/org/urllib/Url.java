package org.urllib;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.urllib.internal.Authority;
import org.urllib.internal.Port;

/**
 * A <a href="https://en.wikipedia.org/wiki/URL">uniform resource locator (Url)</a> that is
 * immutable, compliant with <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>, and
 * interops with Java's {@link URI} and {@link URL}.
 *
 * <h3>The Builder</h3>
 *
 * <p>Use the builder to create a Url from scratch.  For example, this code creates a search
 * for Wolfram Alpha using fancy unicode characters:<pre>{@code
 *
 *   Url url = Url.https("www.wolframalpha.com")
 *                .path("input/")
 *                .query("i", "π²")
 *                .build();
 *
 *   System.out.println(url);
 * }</pre>
 *
 * which prints: <a href="https://www.wolframalpha.com/input/?i=%CF%80%C2%B2">
 * <code>https://www.wolframalpha.com/input/?i=%CF%80%C2%B2</code></a>
 *
 * <h3>java.net.URI</h3>
 *
 * <p>Java's {@link URI} is tough to navigate. If you need a {@link URI}, Urllib has you covered:
 * <pre>{@code
 *
 *   URI moliere = Url.http("wikipedia.org")
 *                    .path("wiki", "Molière")
 *                    .toURI();
 * }</pre>
 *
 * <p>The current implementation supports HTTP and HTTPS URLs, but future implementations may
 * support additional schemes.
 *
 * @since 1.0
 */
public final class Url {

  @Nonnull private final Scheme scheme;
  private final int port;
  @Nonnull private final Authority authority;
  @Nonnull private final Path path;
  @Nonnull private final Query query;
  @Nonnull private final String fragment;

  private Url(Builder builder) {
    this.scheme = builder.scheme;
    this.authority = builder.authority;
    this.port = builder.port;
    this.path = builder.path;
    this.query = builder.query;
    this.fragment = builder.fragment;
  }

  public static Builder http(String host) {
    return new Builder()
        .scheme(Scheme.HTTP)
        .port(Scheme.HTTP.defaultPort())
        .host(host);
  }

  public static Builder https(String host) {
    return new Builder()
        .scheme(Scheme.HTTPS)
        .port(Scheme.HTTPS.defaultPort())
        .host(host);
  }

  /**
   * Returns the Url's scheme.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.1">RFC 3986#3.1</a>
   */
  @Nonnull public Scheme scheme() {
    return scheme;
  }

  /**
   * Returns the Url's host.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2.2">RFC 3986#3.2.2</a>
   */
  @Nonnull public Host host() {
    return authority.host();
  }

  /**
   * Returns the Url's port.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2.3">RFC 3986#3.2.3</a>
   */
  @Nonnegative public int port() {
    return port;
  }

  /**
   * Returns the Url's query.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986#3.3</a>
   */
  @Nonnull public Path path() {
    return path;
  }

  /**
   * Returns the Url's query.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.4">RFC 3986#3.4</a>
   */
  @Nonnull public Query query() {
    return query;
  }

  /**
   * Returns the Url's fragment, not encoded.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.5">RFC 3986#3.5</a>
   */
  @Nonnull public String fragment() {
    return fragment;
  }


  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Url url = (Url) o;

    if (port != url.port) return false;
    if (!scheme.equals(url.scheme)) return false;
    if (!authority.equals(url.authority)) return false;
    if (!path.equals(url.path)) return false;
    if (!query.equals(url.query)) return false;
    return fragment.equals(url.fragment);
  }

  @Override public int hashCode() {
    int result = scheme.hashCode();
    result = 31 * result + port;
    result = 31 * result + authority.hashCode();
    result = 31 * result + path.hashCode();
    result = 31 * result + query.hashCode();
    result = 31 * result + fragment.hashCode();
    return result;
  }

  @Override public String toString() {
    return "Url{" +
        "scheme=" + scheme +
        ", port=" + port +
        ", authority=" + authority +
        ", path=" + path +
        ", query=" + query +
        ", fragment='" + fragment + '\'' +
        '}';
  }

  public static final class Builder {

    @Nonnull private Scheme scheme;
    private int port;
    @Nonnull private Authority authority;
    @Nonnull private Path path = Path.empty();
    @Nonnull private Query query = Query.empty();
    @Nonnull private String fragment = "";

    private Builder() {}

    private Builder scheme(Scheme scheme) {
      this.scheme = scheme;
      return this;
    }

    public Builder port(int port) {
      this.port = Port.validateOrThrow(port);
      return this;
    }

    private Builder host(String host) {
      this.authority = Authority.split(host);
      if (this.authority.port() != -1) {
        port(authority.port());
      }
      return this;
    }

    public Builder path(String... splittableSegments) {
      this.path = Path.of(splittableSegments);
      return this;
    }

    public Builder query(String key, String value) {
      this.query = Query.create(Collections.singletonMap(key, value));
      return this;
    }

    public Builder query(Map<String, String> query) {
      this.query = Query.create(query);
      return this;
    }

    public Builder fragment(String fragment) {
      this.fragment = fragment;
      return this;
    }

    public Url create() {
      return new Url(this);
    }
  }
}
