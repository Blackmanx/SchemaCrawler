/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2021, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package us.fatehi.utility.test.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static us.fatehi.utility.html.TagBuilder.span;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import us.fatehi.utility.Color;
import us.fatehi.utility.html.Tag;
import us.fatehi.utility.html.TagOutputFormat;

public class SpanTest {

  @DisplayName("span: basic output")
  @Test
  public void span1() {
    final Tag span =
        span()
            .withText("display text")
            .withStyleClass("class")
            .withBackground(Color.fromRGB(255, 0, 100))
            .make();
    span.addAttribute("sometag", "customvalue");

    assertThat(span.getTagName(), is("span"));
    assertThat(span.toString(), is("span"));

    assertThat(
        span.render(TagOutputFormat.html),
        is("<span sometag='customvalue' bgcolor='#FF0064' class='class'>display text</span>"));
    assertThat(span.render(TagOutputFormat.text), is("display text"));
    assertThat(span.render(TagOutputFormat.tsv), is("display text"));
  }
}
