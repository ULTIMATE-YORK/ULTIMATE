#!/usr/bin/env python3
"""
Plot ULTIMATE verification results as compact vectorial PDF figures.

Usage:
  plot_results.py FILE
      Single file — one curve (1 param column) or grouped curves (2 param columns).
  plot_results.py FILE1 FILE2
      Dual y-axis plot — both files share the same x-parameter.

Output PDF is written next to each input file.
"""

import os
import sys
import textwrap

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
import numpy as np

try:
    from scipy.interpolate import make_interp_spline
    _SCIPY = True
except ImportError:
    _SCIPY = False

# ── style ──────────────────────────────────────────────────────────────────────
_COLORS  = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#8c564b']
_MARKERS = ['o', 's', '^', 'D', 'v', 'P']

# Width is 50 % wider than the previous 1.8 " baseline.
_FIG_SINGLE = (2.7, 1.3)    # one curve
_FIG_MULTI  = (3.2, 1.5)    # grouped curves — wider + a little taller for legend below
_FIG_DUAL   = (2.7, 1.5)    # dual y-axis

plt.rcParams.update({
    'font.size':         9,
    'axes.linewidth':    0.7,
    'xtick.major.width': 0.7,
    'ytick.major.width': 0.7,
    'xtick.major.size':  3,
    'ytick.major.size':  3,
    'pdf.fonttype':      42,   # TrueType — fully vectorial
})

# ── pretty axis labels ─────────────────────────────────────────────────────────
# Maps the exact formula string (stripped) to a human-readable label.
_LABEL_MAP = {
    'R{"power"}=? [C<=10000]':
        'power use [W]',
    'P=?[F (step=2 & object=2 & detected)]':
        'detection prob.',
    'R{"time"}=?[F "done"]':
        'exec. time [ms]',
    'R{"failures"}=?[F done]':
        'unresolved failures',
    'R{"cost"}=?[F done]':
        'supervisor cost',
    '<<robot1:robot2>>max=? (P[ !"crash" U<=10 "goal1"] + P[ !"crash" U<=10"goal2"])':
        'pSucc1+pSucc2',
}

# ── I/O ────────────────────────────────────────────────────────────────────────

def _read(path):
    with open(path) as fh:
        lines = [ln.rstrip('\n') for ln in fh if ln.strip()]
    sep = '\t' if '\t' in lines[0] else None
    def _sp(s):
        return [t.strip() for t in (s.split(sep) if sep else s.split())]
    headers = _sp(lines[0])
    data = np.array([[float(v) for v in _sp(ln)] for ln in lines[1:]])
    return headers, data

def _out(path, suffix=''):
    return os.path.splitext(path)[0] + suffix + '.pdf'

def _dual_out(p1, p2):
    b1 = os.path.splitext(os.path.basename(p1))[0]
    b2 = os.path.splitext(os.path.basename(p2))[0]
    prefix = os.path.commonprefix([b1, b2]).rstrip('-_. ')
    d = os.path.dirname(p1) or '.'
    return os.path.join(d, (prefix or b1) + '.pdf')

# ── label helpers ──────────────────────────────────────────────────────────────

def _param_label(h):
    return h.split('-', 1)[-1] if '-' in h else h

def _prop_label(h, width=50):
    return '\n'.join(textwrap.wrap(h.strip(), width))

def _pretty(h):
    """Return the human-readable label for h, falling back to wrapped formula."""
    return _LABEL_MAP.get(h.strip(), _prop_label(h))

# ── axis / curve helpers ───────────────────────────────────────────────────────

def _smooth(x, y, n=400):
    if len(x) < 2 or not _SCIPY:
        return x, y
    xs = np.linspace(x[0], x[-1], n)
    return xs, make_interp_spline(x, y, k=min(3, len(x) - 1))(xs)

def _span(arr):
    r = float(np.max(arr) - np.min(arr))
    return max(r, abs(float(np.mean(arr))) * 1e-6, 1e-12)

def _margins(arr, pct=0.05):
    """Return (lo, hi) with pct extra on each side of arr's range."""
    mn, mx = float(np.min(arr)), float(np.max(arr))
    pad = _span(arr) * pct
    return mn - pad, mx + pad

def _dual_ylims(y1, y2, pct=0.05):
    """Place y1 in the bottom 3/4 of ax1 and y2 in the top 3/4 of ax2,
    guaranteeing ~25 % vertical separation for any monotone pair.
    Also adds pct margin on the 'exposed' side of each axis."""
    r1, r2 = _span(y1), _span(y2)
    lim1 = (float(np.min(y1)) - r1 * pct,  float(np.max(y1)) + r1 / 3)
    lim2 = (float(np.min(y2)) - r2 / 3,    float(np.max(y2)) + r2 * pct)
    return lim1, lim2

def _setup_ticks(ax, x, nbins_x=5, nbins_y=4):
    """Set tick locators and suppress every-other x label when > 6 ticks."""
    is_int = bool(np.all(x == np.floor(x)))
    ax.xaxis.set_major_locator(mticker.MaxNLocator(nbins=nbins_x, integer=is_int))
    ax.yaxis.set_major_locator(mticker.MaxNLocator(nbins=nbins_y))
    ax.get_figure().canvas.draw()
    xlabels = ax.get_xticklabels()
    if len(xlabels) > 6:
        for lbl in xlabels[1::2]:
            lbl.set_visible(False)

def _add_grid(ax):
    ax.set_axisbelow(True)
    ax.grid(True, color='#cccccc', linewidth=0.4, alpha=0.8)

def _save(fig, path):
    fig.savefig(path, format='pdf', bbox_inches='tight')
    plt.close(fig)
    print(f'Saved: {path}')

# ── plot routines ──────────────────────────────────────────────────────────────

def plot_single(path):
    headers, data = _read(path)
    nparams = data.shape[1] - 1

    if nparams == 1:
        fig, ax = plt.subplots(figsize=_FIG_SINGLE)
        x, y = data[:, 0], data[:, 1]
        ax.plot(*_smooth(x, y), color=_COLORS[0], lw=1.2)
        ax.plot(x, y, marker=_MARKERS[0], color=_COLORS[0], ls='', ms=4)
        ax.set_xlabel(_param_label(headers[0]), fontsize=9)
        ax.set_ylabel(_pretty(headers[1]), fontsize=8)
        ax.set_xlim(*_margins(x))
        ax.set_ylim(*_margins(y))
        ax.tick_params(labelsize=8)
        _setup_ticks(ax, x)
        _add_grid(ax)
        fig.tight_layout(pad=0.3)

    elif nparams == 2:
        fig, ax = plt.subplots(figsize=_FIG_MULTI)
        groups = np.unique(data[:, 0])
        for i, g in enumerate(groups):
            mask = data[:, 0] == g
            x, y = data[mask, 1], data[mask, 2]
            c = _COLORS[i % len(_COLORS)]
            m = _MARKERS[i % len(_MARKERS)]
            ax.plot(*_smooth(x, y), color=c, lw=1.2,
                    label=f'{_param_label(headers[0])}={g:.3g}')
            ax.plot(x, y, marker=m, color=c, ls='', ms=4)
        ax.set_xlabel(_param_label(headers[1]), fontsize=9)
        ax.set_ylabel(_pretty(headers[2]), fontsize=8)
        ax.set_xlim(*_margins(data[:, 1]))
        ax.set_ylim(*_margins(data[:, 2]))
        ax.tick_params(labelsize=8)
        _setup_ticks(ax, data[:, 1])
        _add_grid(ax)
        # Legend in three columns below the axes
        ax.legend(loc='upper center', bbox_to_anchor=(0.5, -0.28),
                  ncol=3, fontsize=7, framealpha=0.85,
                  borderpad=0.4, columnspacing=0.8, handlelength=1.5)
        fig.tight_layout(pad=0.3)

    else:
        sys.exit(f'Unsupported column count in {path}')

    _save(fig, _out(path))


def plot_dual(path1, path2):
    headers1, data1 = _read(path1)
    headers2, data2 = _read(path2)
    x1, y1 = data1[:, 0], data1[:, 1]
    x2, y2 = data2[:, 0], data2[:, 1]

    lim1, lim2 = _dual_ylims(y1, y2)

    fig, ax1 = plt.subplots(figsize=_FIG_DUAL)
    ax2 = ax1.twinx()

    ax1.plot(*_smooth(x1, y1), color=_COLORS[0], lw=1.2)
    ax1.plot(x1, y1, marker=_MARKERS[0], color=_COLORS[0], ls='', ms=4)
    ax2.plot(*_smooth(x2, y2), color=_COLORS[1], lw=1.2)
    ax2.plot(x2, y2, marker=_MARKERS[1], color=_COLORS[1], ls='', ms=4)

    ax1.set_xlim(*_margins(x1))
    ax1.set_ylim(*lim1)
    ax2.set_ylim(*lim2)

    ax1.set_xlabel(_param_label(headers1[0]), fontsize=9)
    ax1.set_ylabel(_pretty(headers1[1]), fontsize=8, color=_COLORS[0])
    ax2.set_ylabel(_pretty(headers2[1]), fontsize=8, color=_COLORS[1])
    ax1.tick_params(axis='y', labelcolor=_COLORS[0], labelsize=8)
    ax2.tick_params(axis='y', labelcolor=_COLORS[1], labelsize=8)
    ax1.tick_params(axis='x', labelsize=8)

    ax1.yaxis.set_major_locator(mticker.MaxNLocator(nbins=4))
    ax2.yaxis.set_major_locator(mticker.MaxNLocator(nbins=4))
    _setup_ticks(ax1, x1)
    _add_grid(ax1)

    fig.tight_layout(pad=0.3)
    _save(fig, _dual_out(path1, path2))


# ── entry point ────────────────────────────────────────────────────────────────

if __name__ == '__main__':
    if len(sys.argv) == 2:
        plot_single(sys.argv[1])
    elif len(sys.argv) == 3:
        plot_dual(sys.argv[1], sys.argv[2])
    else:
        sys.exit(f'Usage: {sys.argv[0]} FILE [FILE2]')
