package com.ljs.ifootballmanager.ai.report;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.CharSink;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/** @author lstephen */
public final class Reports {

  private Reports() {}

  public static Destination print(final Report r) {
    return new Destination() {
      @Override
      public void to(OutputStream out) {
        to(new OutputStreamWriter(out, Charsets.ISO_8859_1));
      }

      @Override
      public void to(Writer w) {
        PrintWriter pw = new PrintWriter(w);
        r.print(pw);
        pw.flush();
      }

      @Override
      public void to(CharSink sink) {
        try (Writer w = sink.openStream()) {
          to(w);
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }
    };
  }

  public interface Destination {
    void to(OutputStream out);

    void to(Writer w);

    void to(CharSink sink);
  }
}
