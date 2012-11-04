//
//  RootViewController.m
//  paperless
//
//  Created by Matt Smollinger on 11/3/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import "RootViewController.h"
#import "FileView.h"
#import "AppDelegate.h"
#import "BaseViewController.h"

@interface RootViewController ()

@property (nonatomic, strong) FileView *fileView;

@property (nonatomic, strong) BaseViewController *splitView;

@end

@implementation RootViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)loadView
{
    AppDelegate *delegate = [UIApplication sharedApplication].delegate;
    self.view = [[UIView alloc] initWithFrame:delegate.window.bounds];
//    self.fileView = [[FileView alloc] init];
//    self.fileView.backgroundImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ooo.png"]];
    self.splitView = [[BaseViewController alloc] init];
    [self.view addSubview:self.splitView.view];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
