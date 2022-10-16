# requirements:
# pip install huggingface_hub
# git config --global credential.helper store

import argparse
from huggingface_hub import InferenceApi

class Bloom:
    def arguments_reader(self):
        parser = argparse.ArgumentParser(description='All the weights of the Bloom model and the message')
        parser.add_argument('-n', '--min-length',  required=False, type=int,   default=0,  help='Integer to define the minimum length in tokens of the output summary.')
        parser.add_argument('-m', '--max-length',  required=False, type=int,   default=32,  help='Integer to define the maximum length in tokens of the output summary.')
        parser.add_argument('-k', '--top-k',       required=False, type=float, default=0,  help='Integer to define the top tokens considered within the sample operation to create new text.')
        parser.add_argument('-b', '--beams',       required=False, type=int,   default=0,  help='Number of Beams')
        parser.add_argument('-r', '--no-repeat',   required=False, type=int,   default=2,     help='No repeat ngram size')
        parser.add_argument('-p', '--top-p',       required=False, type=float, default=.9,  help='Float to define the tokens that are within the sample operation of text generation. Add tokens in the sample for more probable to least probable until the sum of the probabilities is greater than top_p.')
        parser.add_argument('-t', '--temperature', required=False, type=float, default=.7,     help='Float (0.0-100.0). The temperature of the sampling operation. 1 means regular sampling, 0 means always take the highest score, 100.0 is getting closer to uniform probability.')
        parser.add_argument('-s', '--seed',        required=False, type=int,   default=42,    help='Seed')
        parser.add_argument('-d', '--decoding',    required=False, type=bool,  default=False, help='Greedy Decoding')
        parser.add_argument('-f', '--full-text',   required=False, type=bool,  default=False, help='Return full text')
        parser.add_argument('-a', '--token',       required=True,  type=str,                  help='Hugging Face access token')
        parser.add_argument('-i', '--input',       required=True,  type=str,                  help='A string to be summarized')
        return parser.parse_args()

    def bloom_prompt(self,
                     prompt,
                     max_length,
                     top_k,
                     num_beams,
                     no_repeat_ngram_size,
                     top_p,
                     seed,
                     temperature,
                     greedy_decoding,
                     return_full_text):
        params = {
            "max_new_tokens":       max_length,
            "top_k":                None if top_k == 0 else top_k,
            "top_p":                None if num_beams else top_p,
            "temperature":          temperature,
            "do_sample":            False if num_beams > 0 else not greedy_decoding,
            "seed":                 seed,
            "early_stopping":       None if num_beams is None else num_beams > 0,
            "no_repeat_ngram_size": None if num_beams is None else no_repeat_ngram_size,
            "num_beams":            None if (greedy_decoding or num_beams == 0) else num_beams,
            "return_full_text":     return_full_text
        }

        return self.inference(prompt, params=params)

    def __init__(self):
        self = self
        self.arg = self.arguments_reader()
        self.token = self.arg.token
        self.inference = InferenceApi(repo_id="bigscience/bloom", token=self.token)

    def __call__(self):
        response = self.bloom_prompt(prompt=self.arg.input,
                                     max_length=self.arg.max_length,
                                     top_k=self.arg.top_k,
                                     num_beams=self.arg.beams,
                                     no_repeat_ngram_size=self.arg.no_repeat,
                                     top_p=self.arg.top_p,
                                     seed=self.arg.seed,
                                     temperature=self.arg.temperature,
                                     greedy_decoding=self.arg.decoding,
                                     return_full_text=self.arg.full_text)
        return response

bloom = Bloom()
print(bloom())