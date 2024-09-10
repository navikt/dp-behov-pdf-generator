package no.nav.dagpenger.pdf.html

internal fun css(saksnummer: String): String {
    // language=CSS
    return """
        .melding-om-vedtak {
            width: 595px; /* A4 bredde */
            height: 842px; /* A4 høyde */
            padding: 64px 64px 74px;

            box-shadow: 0px 5px 12px 0px rgba(0, 0, 0, 0.13), 0px 1px 3px 0px rgba(0, 0, 0, 0.10), 0px 0px 1px 0px rgba(0, 0, 0, 0.15);
            background-color: white;
            overflow: auto;

            font-family: 'Source Sans 3', sans-serif;
            font-size: 11px;
            line-height: 16px;
            font-weight: normal;
        }

        .melding-om-vedtak__logo {
            margin-bottom: 48px;
        }

        .melding-om-vedtak__header {
            margin-bottom: 48px;
        }

        .melding-om-vedtak__header p {
            font-size: 11px;
            line-height: 16px;
            font-weight: normal;
            margin: 0;
        }

        .melding-om-vedtak__saksnummer-dato {
            overflow: hidden; /* Ensures container wraps around floated items */
        }

        .melding-om-vedtak__saksnummer-dato .left {
            float: left; /* Aligns element to the left */
        }

        .melding-om-vedtak__saksnummer-dato .right {
            float: right; /* Aligns element to the right */
        }

        .meldingOmVedtak__tekst-blokk:not(:first-of-type) {
            margin-bottom: 26px;
        }

        .meldingOmVedtak__tekst-blokk h1 {
            font-size: 16px;
            line-height: 20px;
            font-weight: bold;
            margin: 0 0 26px 0;
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
        }

        .meldingOmVedtak__tekst-blokk h3 {
            font-size: 12px;
        }

        .meldingOmVedtak__tekst-blokk h4 {
            font-size: 11px;
        }

        .meldingOmVedtak__tekst-blokk p {
            font-size: 11px;
            line-height: 16px;
            font-weight: normal;
        }

        .meldingOmVedtak__signatur {
            margin: 32px 0 40px;
        }

        @page {
            size: A4 portrait;
            @bottom-right {
                content: 'side ' counter(page) ' av ' counter(pages);
                font-family: 'Source Sans 3', serif;
                font-size: 9px;
                padding-bottom: 26px;
                padding-right: 39px;
            }

            @bottom-left {
                content: 'Saksnummer: $saksnummer';
                font-family: 'Source Sans 3', serif;
                font-size: 9px;
                padding-bottom: 26px;
                padding-left: 71px;
            }
            orphans: 2;
        }

        @media print {
            h2, h3 {
                page-break-after: avoid
            }
        }
        """.trimIndent()
}
