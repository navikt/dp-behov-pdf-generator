package no.nav.dagpenger.pdf.html

internal fun css(saksnummer: String): String {
    // language=CSS
    return """
        @import url('https://fonts.googleapis.com/css2?family=Source+Sans+3:ital,wght@0,200..900;1,200..900&display=swap');
                
        .melding-om-vedtak {
            font-family: 'Source Sans 3', sans-serif;
            font-size: 11px;
            line-height: 16px;
            font-weight: normal;
        }

        .melding-om-vedtak__header,
        .melding-om-vedtak__logo {
            margin-bottom: 48px;
        }

        .melding-om-vedtak__header p {
            font-size: 11px;
            line-height: 16px;
            font-weight: normal;
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
            font-size: 16px;
            line-height: 20px;
            font-weight: bold;
            margin: 0 0 26px 0;
            letter-spacing: 0.3px;
        }

        .meldingOmVedtak__tekst-blokk h2,
        .meldingOmVedtak__tekst-blokk h3,
        .meldingOmVedtak__tekst-blokk h4 {
            line-height: 16px;
            font-weight: bold;
            margin: 0 0 6px 0;
        }

        .meldingOmVedtak__tekst-blokk h2 {
            font-size: 13px;
            letter-spacing: 0.25px;
        }

        .meldingOmVedtak__tekst-blokk h3 {
            font-size: 12px;
            letter-spacing: 0.2px;
        }

        .meldingOmVedtak__tekst-blokk h4 {
            font-size: 11px;
            letter-spacing: 0.1px;
        }

        .meldingOmVedtak__tekst-blokk p {
            font-size: 11px;
            line-height: 16px;
            font-weight: normal;
        }

        .meldingOmVedtak__signatur {
            margin-top: 32px;
        }

        @page {
            width: 200px;
            height: 200px;
            @bottom-right {
                content: 'side ' counter(page) ' av ' counter(pages);
                font-family: 'Source Sans 3', serif;
                font-size: 9px;
                padding-bottom: 26px;
                padding-right: 8px;
            }

            @bottom-left {
                content: 'Saksnummer: $saksnummer';
                font-family: 'Source Sans 3', serif;
                font-size: 9px;
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
        """.trimIndent()
}
