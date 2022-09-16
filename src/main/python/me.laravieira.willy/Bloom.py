# requirements:
# pip install huggingface_hub
# git config --global credential.helper store

import time
import argparse

from huggingface_hub import HfFolder
from huggingface_hub import InferenceApi

inference = InferenceApi("bigscience/bloom",token=HfFolder.get_token())

def arguments_reader():
    parser = argparse.ArgumentParser(description='All the weights of the Bloom model and the message')
    parser.add_argument('-m', '--max-length', help='The max numbers of tokens to return', required=False, default=32)
    parser.add_argument('-k', '--top-k', help='Top K', required=False, default=0)
    parser.add_argument('-b', '--beams', help='Number of Beams', required=False, default=0)
    parser.add_argument('-r', '--no-repeat', help='No repeat ngram size', required=False, default=2)
    parser.add_argument('-p', '--top-p', help='Top P', required=False, default=0.9)
    parser.add_argument('-t', '--temperature', help='Temperature', required=False, default=0.7)
    parser.add_argument('-s', '--seed', help='Seed', required=False, default=42)
    parser.add_argument('-d', '--greedy-decoding', help='Greedy Decoding', required=False, default=False)
    parser.add_argument('-f', '--full-text', help='Return full text', required=False, default=False)
    parser.add_argument('-c', '--content', help='The prompt text', required=True)
    return parser.parse_args()

def bloom_prompt(prompt,
          max_length = 32,
          top_k = 0,
          num_beams = 0,
          no_repeat_ngram_size = 2,
          top_p = 0.9,
          seed=42,
          temperature=0.7,
          greedy_decoding = False,
          return_full_text = False):

    top_k = None if top_k == 0 else top_k
    do_sample = False if num_beams > 0 else not greedy_decoding
    num_beams = None if (greedy_decoding or num_beams == 0) else num_beams
    no_repeat_ngram_size = None if num_beams is None else no_repeat_ngram_size
    top_p = None if num_beams else top_p
    early_stopping = None if num_beams is None else num_beams > 0

    params = {
        "max_new_tokens": max_length,
        "top_k": top_k,
        "top_p": top_p,
        "temperature": temperature,
        "do_sample": do_sample,
        "seed": seed,
        "early_stopping":early_stopping,
        "no_repeat_ngram_size":no_repeat_ngram_size,
        "num_beams":num_beams,
        "return_full_text":return_full_text
    }

    return inference(prompt, params=params)

arg = arguments_reader()
response = bloom_prompt(prompt=arg.content,
                        max_length=arg.max_length,
                        top_k=arg.top_k,
                        num_beams=arg.beams,
                        no_repeat_ngram_size=arg.no_repeat,
                        top_p=arg.top_p,
                        seed=arg.seed,
                        temperature=arg.temperature,
                        greedy_decoding=arg.greedy_decoding,
                        return_full_text=arg.full_text)
print(response)
