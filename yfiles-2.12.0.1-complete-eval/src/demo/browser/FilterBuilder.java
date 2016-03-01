/****************************************************************************
 * This demo file is part of yFiles for Java 2.12.0.1.
 * Copyright (c) 2000-2016 by yWorks GmbH, Vor dem Kreuzberg 28,
 * 72070 Tuebingen, Germany. All rights reserved.
 * 
 * yFiles demo files exhibit yFiles for Java functionalities. Any redistribution
 * of demo files in source code or binary form, with or without
 * modification, is not permitted.
 * 
 * Owners of a valid software license for a yFiles for Java version that this
 * demo is shipped with are allowed to use the demo source code as basis
 * for their own yFiles for Java powered applications. Use of such programs is
 * governed by the rights and conditions as set out in the yFiles for Java
 * license agreement.
 * 
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL yWorks BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***************************************************************************/
package demo.browser;

import java.util.Iterator;
import java.util.Set;

/**
 * Builder for text based filters.
 *
 */
class FilterBuilder {
  public static final byte MATCH_ANYWHERE   = 0;
  public static final byte MATCH_EXACTLY    = 1;
  public static final byte MATCH_FROM_START = 2;

  public static final int DOMAIN_NAMES = 1;
  public static final int DOMAIN_DESCRIPTION = 2;
  public static final int DOMAIN_SUMMARY = 4;
  public static final int DOMAIN_KEYWORDS = 8;

  private int domain;
  private byte matchMode;
  private boolean caseSensitive;
  private String needle = "";

  FilterBuilder() {
    domain = -1;
  }

  FilterBuilder addDomain( final int domainFlag ) {
    this.domain |= domainFlag;
    return this;
  }

  FilterBuilder removeDomain( final int domainFlag ) {
    this.domain &= ~domainFlag;
    return this;
  }

  FilterBuilder setMatchMode( final byte matchMode ) {
    switch (matchMode) {
      case MATCH_ANYWHERE:
      case MATCH_EXACTLY:
      case MATCH_FROM_START:
        this.matchMode = matchMode;
        break;
      default:
        throw new IllegalArgumentException("Unsupported mode: " + matchMode);
    }
    return this;
  }

  FilterBuilder setCaseSensitive( final boolean caseSensitive ) {
    this.caseSensitive = caseSensitive;
    return this;
  }

  FilterBuilder setNeedle( final String needle ) {
    if (needle == null) {
      throw new IllegalArgumentException("null");
    }
    this.needle = needle;
    return this;
  }

  Filter build() {
    if ("".equals(needle)) {
      return null;
    } else {
      return new Filter() {
        public boolean accept( final Displayable data ) {
          if (isDomainSet(DOMAIN_NAMES)) {
            if (data.isDemo()) {
              final String simpleName = getSimpleName(data.getQualifiedName());
              if (simpleName != null) {
                if (match(simpleName, needle)) {
                  return true;
                }

                if (match(selectUpperCaseCharsOf(simpleName), needle)) {
                  return true;
                }
              }
            }

            final String displayName = data.getDisplayName();
            if (displayName != null && match(displayName, needle)) {
              return true;
            }
          }

          if (isDomainSet(DOMAIN_DESCRIPTION)) {
            final String description = HtmlUtil.getText(data.getDescription());
            if (description != null && match(description, needle)) {
              return true;
            }
          }

          if (isDomainSet(DOMAIN_SUMMARY)) {
            final String summary = HtmlUtil.getText(data.getSummary());
            if (summary != null && match(summary, needle)) {
              return true;
            }
          }

          if (isDomainSet(DOMAIN_KEYWORDS)) {
            final Set keywords = data.getKeywords();
            if (keywords != null && match(keywords, needle)) {
              return true;
            }
          }
          return false;
        }

        boolean isDomainSet( final int flag ) {
          return (domain & flag) == flag;
        }

        boolean match( String haystack, String needle ) {
          if (!caseSensitive) {
            haystack = haystack.toLowerCase();
            needle = needle.toLowerCase();
          }

          switch (matchMode) {
            case MATCH_ANYWHERE:
              return haystack.indexOf(needle) > -1;
            case MATCH_EXACTLY:
              return haystack.equals(needle);
            case MATCH_FROM_START:
              return haystack.startsWith(needle);
            default:
              throw new IllegalStateException();
          }
        }

        boolean match( final Set haystacks, final String needle ) {
          for (Iterator it = haystacks.iterator(); it.hasNext(); ) {
            final String haystack = (String) it.next();
            if (match(haystack, needle)) {
              return true;
            }
          }
          return false;
        }

        String selectUpperCaseCharsOf(final String text) {
          final StringBuffer sb = new StringBuffer();
          for (int i = 0; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (Character.isUpperCase(ch)) {
              sb.append(ch);
            }
          }
          return sb.toString();
        }

        String getSimpleName( final String qualifiedName ) {
          final int idx = qualifiedName.lastIndexOf('.');
          if (idx > -1) {
            return qualifiedName.substring(idx + 1);
          } else {
            return qualifiedName;
          }
        }
      };
    }
  }
}
