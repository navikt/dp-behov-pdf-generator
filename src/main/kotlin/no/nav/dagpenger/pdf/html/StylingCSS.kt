package no.nav.dagpenger.pdf.html

internal fun css(saksnummer: String): String {
    // language=CSS
    return """
        .melding-om-vedtak {
            font-family: 'Source Sans 3', sans-serif;
            font-size: 11pt;
            line-height: 16pt;
            font-weight: 400;
        }

        .melding-om-vedtak__header,
        .melding-om-vedtak__logo {
            margin-bottom: 48px;
        }

        .melding-om-vedtak__header p {
            font-size: 11pt;
            line-height: 16pt;
            font-weight: 400;
            margin: 0;
        }
        
        .melding-om-vedtak__opplysning-verdi {
          white-space: nowrap;
        }

        .melding-om-vedtak__saksnummer-dato {
            overflow: hidden; /* Clear the floats */
        }
        
        .melding-om-vedtak__saksnummer-dato--left {
            float: left;
        }
        
        .melding-om-vedtak__saksnummer-dato--right {
            float: right;
        }

          .meldingOmVedtak__tekst-blokk {
            margin-bottom: 26px;
          }
          
          .meldingOmVedtak__tekst-blokk--first {
            margin-bottom: 0;
          }

        .meldingOmVedtak__tekst-blokk h1 {
            font-size: 16pt;
            line-height: 20pt;
            font-weight: 700;
            margin: 0 0 26px 0;
            letter-spacing: 0.3px;
        }

        .meldingOmVedtak__tekst-blokk h2,
        .meldingOmVedtak__tekst-blokk h3,
        .meldingOmVedtak__tekst-blokk h4 {
            line-height: 16pt;
            font-weight: 700;
            margin: 0 0 6px 0;
        }

        .meldingOmVedtak__tekst-blokk h2 {
            font-size: 13pt;
            letter-spacing: 0.25px;
        }

        .meldingOmVedtak__tekst-blokk h3 {
            font-size: 12pt;
            letter-spacing: 0.2px;
        }

        .meldingOmVedtak__tekst-blokk h4 {
            font-size: 11pt;
            letter-spacing: 0.1px;
        }

        .meldingOmVedtak__tekst-blokk p {
            font-size: 11pt;
            line-height: 16pt;
            font-weight: 400;
        }

        .meldingOmVedtak__signatur {
            margin-top: 32px;
        }

        @page {
            padding-bottom: 26px;
            
            @bottom-right {
                content: 'side ' counter(page) ' av ' counter(pages);
                font-family: 'Source Sans 3', serif;
                font-size: 9pt;
                padding-bottom: 26px;
                padding-right: 8px;
            }

            @bottom-left {
                content: 'Saksnummer: $saksnummer';
                font-family: 'Source Sans 3', serif;
                font-size: 9pt;
                padding-bottom: 26px;
                padding-left: 8px;
            }
        }

        @media print {
            .meldingOmVedtak__tekst-blokk h1,
            .meldingOmVedtak__tekst-blokk h2,
            .meldingOmVedtak__tekst-blokk h3,
            .meldingOmVedtak__tekst-blokk h4 {
                page-break-after: avoid;
            }
        }
        
        input {
            min-height: 1.1em; /* Resolves getControlFont IndexOutOfBoundsException */
        }
        """.trimIndent()
}
